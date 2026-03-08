# 実装ルール

---

## 1. レイヤー構成

```
Controller → Service → Repository → jOOQ (DB)
```

| レイヤー | パッケージ | 役割 |
|---|---|---|
| Controller | `controller/` | HTTPリクエスト受付・レスポンス返却 |
| DTO | `controller/dto/` | リクエスト・レスポンスのデータ構造 |
| Service | `service/` | ビジネスロジック |
| Repository | `repository/` | DBアクセス（jOOQ使用） |
| Domain | `domain/` | 純粋なデータクラス（jOOQ非依存） |
| Exception | `exception/` | カスタム例外クラス |

---

## 2. ドメインモデル

- jOOQ 生成クラスに依存しない純粋な `data class` とする
- `id` は `Long?`（登録前は `null`）
- ファイル: `src/main/kotlin/com/example/library_management/domain/`

```kotlin
data class Foo(
    val id: Long?,
    val field1: String,
    val field2: LocalDate,
)
```

---

## 3. DTO

### リクエストDTO

- フィールドはすべて nullable（`String?`, `LocalDate?` など）にする
- バリデーションアノテーションを `@field:` プレフィックスで付与する
- バリデーションは **DTOアノテーションのみ** で行い、Service 層で重複実装しない

```kotlin
data class FooRequest(
    @field:NotBlank
    val name: String?,
    @field:NotNull
    @field:PastOrPresent
    val birthDate: LocalDate?,
)
```

代表的なバリデーションアノテーション:

| 条件 | アノテーション |
|---|---|
| 空文字不可（String） | `@field:NotBlank` |
| null不可 | `@field:NotNull` |
| 現在日以前 | `@field:PastOrPresent` |
| 0以上の数値 | `@field:Min(0)` |
| リストの最低件数 | `@field:Size(min = 1)` |

### レスポンスDTO

- フィールドは non-null で定義する

```kotlin
data class FooResponse(
    val id: Long,
    val field1: String,
    val field2: LocalDate,
)
```

---

## 4. Repository

- `@Repository` アノテーションを付与する
- `DSLContext` をコンストラクタ DI する
- jOOQ 生成クラス（`Tables.XXX`）を使ってクエリを記述する
- Repository は DB アクセスのみを担い、存在チェックや例外スローは行わない
- `findById` は存在しない場合に `null` を返す

```kotlin
@Repository
class FooRepository(private val dsl: DSLContext) {

    fun insert(foo: Foo): Foo { ... }

    fun update(foo: Foo): Foo {
        dsl.update(FOO)...execute()
        return foo
    }

    fun findById(id: Long): Foo? { ... }
}
```

---

## 5. Service

- `@Service` アノテーションを付与する
- Repository をコンストラクタ DI する
- ビジネスルール違反（状態遷移違反など）は `IllegalArgumentException` をスローする
- 存在チェック・`Exception` のスローは **Controller の責務** とし、Service では行わない
- バリデーションは DTO アノテーションに委譲し、Service で再実装しない（DTO で表現できないビジネスルールのみ実装する）
- リソースの存在確認用に `findById(id): FooResponse?` メソッドを公開し、Controller から呼び出せるようにする

```kotlin
@Service
class FooService(private val fooRepository: FooRepository) {

    fun findById(id: Long): FooResponse? = fooRepository.findById(id)?.let { toResponse(it) }

    fun create(request: FooRequest): FooResponse { ... }
    fun update(id: Long, request: FooRequest): FooResponse { ... }
}
```

---

## 6. Controller

- `@RestController` + `@RequestMapping("/path")` を付与する
- Service をコンストラクタ DI する
- リクエストボディには `@Valid @RequestBody` を付与する
- パスパラメータには `@PathVariable` を使用する
- HTTPステータスは `@ResponseStatus` で明示する

| 操作 | メソッド | ステータス | レスポンスボディ |
|---|---|---|---|
| 登録（POST） | `@PostMapping` | `@ResponseStatus(HttpStatus.CREATED)` | `{"id": <id>}` のみ |
| 更新（PUT） | `@PutMapping("/{id}")` | `@ResponseStatus(HttpStatus.NO_CONTENT)` | なし |
| 取得（GET） | `@GetMapping` | （デフォルト 200） | リソース本体 |

- 更新系エンドポイントでは **すべての更新処理より前に** 精査処理（項目間精査やDB相関精査も含む）を行い、対応するException`をスローする
- `Exception` のスローは Controller の責務であり、Service・Repository では行わない

```kotlin
@RestController
@RequestMapping("/foos")
class FooController(private val fooService: FooService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: FooRequest): Map<String, Long> {
        val response = fooService.create(request)
        return mapOf("id" to response.id)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Long, @Valid @RequestBody request: FooRequest) {
        fooService.findById(id) ?: throw NotFoundException("Foo not found: id=$id")
        fooService.update(id, request)
    }
}
```

---

## 7. 例外ハンドリング

`GlobalExceptionHandler`が以下のマッピングを提供する。追加は不要。

| 例外クラス | HTTPステータス |
|---|---|
| `MethodArgumentNotValidException` | 400 Bad Request |
| `IllegalArgumentException` | 400 Bad Request |
| `NotFoundException` | 404 Not Found |

レスポンスボディ形式:
```json
{ "error": "<メッセージ>" }
```

---

## 8. 単体テスト

### ServiceTest

- `@SpringBootTest` + `@Transactional` を使用した統合テスト（実際の DB に対して検証する）
- `@Transactional` によりテスト後はロールバックされるため、テスト間の干渉がない
- Service と Repository は DI で注入し、モックは使用しない
- 正常系では戻り値の検証に加え、`Repository.findById` で DB の実データも検証する

```kotlin
@SpringBootTest
@Transactional
class FooServiceTest {
    @Autowired lateinit var fooService: FooService
    @Autowired lateinit var fooRepository: FooRepository

    @Test
    fun `create - DBに保存されること`() {
        val response = fooService.create(...)

        // レスポンス検証
        assertNotNull(response.id)

        // DB検証
        val saved = fooRepository.findById(response.id)
        assertNotNull(saved)
        assertEquals("期待値", saved!!.field)
    }
}
```

### ControllerTest

- `@WebMvcTest(FooController::class)` を使用し MockMvc でHTTPレベルのテスト
- Service は `@MockkBean` でモック化

```kotlin
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import com.ninjasquad.springmockk.MockkBean

@WebMvcTest(FooController::class)
class FooControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var fooService: FooService
    ...
}
```

テストケースの最低要件:

| テスト対象 | 正常系 | 異常系 |
|---|---|---|
| POST エンドポイント | 201 が返ること | バリデーションエラーで 400 が返ること |
| PUT エンドポイント | 204 が返ること | バリデーションエラーで 400 が返ること、存在しない ID で 404 が返ること |
| GET エンドポイント | 200 と期待データが返ること | 存在しない ID で 404 が返ること |

---

## 9. REST Client ファイル

- `docs/api/<resource>.http` に記述する
- `@baseUrl = http://localhost:8080` を先頭に定義する
- 各リクエストに日本語コメント（`### 説明`）を付ける
- 正常系・バリデーションエラー・404 の各シナリオを網羅する

```http
@baseUrl = http://localhost:8080

### POST /foos - 登録（正常系）
POST {{baseUrl}}/foos
Content-Type: application/json

{ ... }
```
