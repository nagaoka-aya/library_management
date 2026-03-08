# Task 3: 著者登録・更新API実装 + 単体テスト

## 概要
著者の登録・更新APIを実装し、単体テストを作成する。

---

## 作業内容

### 1. ドメインモデル・DTO 作成

#### 入力

- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/domain/Author.kt`
  - `Author` データクラス（id, name, birthDate）
- `src/main/kotlin/com/example/library_management/controller/dto/AuthorRequest.kt`
  - `AuthorRequest` データクラス（name, birthDate）
- `src/main/kotlin/com/example/library_management/controller/dto/AuthorResponse.kt`
  - `AuthorResponse` データクラス（id, name, birthDate）

#### 備考

- ドメインモデルは jOOQ の生成クラスに依存しない純粋なデータクラスとする
- バリデーションアノテーション（`@NotBlank`, `@Past` など）はリクエストDTOに付与する

---

### 2. AuthorRepository 実装

#### 入力

- `docs/spec/db_structure.md`（Task 1-1 の成果物）
- jOOQ 生成コード（Task 2-3 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/repository/AuthorRepository.kt`
  - `insert(author: Author): Author` — 著者登録
  - `update(author: Author): Author` — 著者更新
  - `findById(id: Long): Author?` — IDで著者取得

#### 備考

- jOOQ の生成クラス（`Tables.AUTHORS`）を使ってクエリを記述する
- `DSLContext` を DI して使用する
- 存在しない ID への更新は `IllegalArgumentException` をスローする

---

### 3. AuthorService 実装

#### 入力

- `AuthorRepository`（本タスク 3-2 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/service/AuthorService.kt`
  - `create(request: AuthorRequest): AuthorResponse` — 著者登録
  - `update(id: Long, request: AuthorRequest): AuthorResponse` — 著者更新

#### 備考

- バリデーション：`birthDate` が現在日以前であることをサービス層で検証する
- バリデーション違反時は `IllegalArgumentException` をスローする
- `AuthorRepository` を DI して使用する

---

### 4. AuthorController 実装

#### 入力

- `AuthorService`（本タスク 3-3 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/controller/AuthorController.kt`
  - `POST /authors` — 著者登録
  - `PUT /authors/{id}` — 著者更新
- `src/main/kotlin/com/example/library_management/controller/GlobalExceptionHandler.kt`
  - `IllegalArgumentException` を 400 Bad Request に変換する `@RestControllerAdvice`

#### 備考

- `@RestController` / `@RequestBody` / `@PathVariable` / `@Valid` を使用する
- リクエストボディのバリデーションエラー（`@Valid` 違反）も 400 Bad Request を返す
- `springdoc-openapi` の `pom.xml` への依存追加もこのタイミングで行う

---

### 5. REST Client ファイル作成

#### 入力

- `AuthorController`（本タスク 3-4 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `docs/api/author.http`
  - `POST /authors` — 著者登録リクエスト例（正常系・バリデーションエラー例）
  - `PUT /authors/{id}` — 著者更新リクエスト例（正常系・バリデーションエラー例）

#### 備考

- VS Code の REST Client 拡張（`humao.rest-client`）で実行できる形式で記述する
- `@baseUrl = http://localhost:8080` のような変数定義を先頭に置く
- アプリを起動した状態で実行し、期待通りのレスポンスが返ることを手動確認する

---

### 6. 単体テスト作成

#### 入力

- `AuthorService`（本タスク 3-3 の成果物）
- `AuthorController`（本タスク 3-4 の成果物）

#### 出力

- `src/test/kotlin/com/example/library_management/service/AuthorServiceTest.kt`
  - 正常系：著者登録・更新が成功すること
  - 異常系：`birthDate` が未来日の場合に例外がスローされること
  - 異常系：存在しない ID への更新で例外がスローされること
- `src/test/kotlin/com/example/library_management/controller/AuthorControllerTest.kt`
  - 正常系：POST `/authors` が 201 を返すこと
  - 正常系：PUT `/authors/{id}` が 200 を返すこと
  - 異常系：バリデーションエラー時に 400 を返すこと

#### 備考

- `AuthorServiceTest` は `@ExtendWith(MockKExtension::class)` を使用した純粋なユニットテストとする
- `AuthorControllerTest` は `@WebMvcTest(AuthorController::class)` を使用し MockMvc でHTTPレベルのテストを行う
- `AuthorService` のモックには `mockk<AuthorService>()` を使用する
