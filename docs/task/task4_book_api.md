# Task 4: 書籍登録・更新API実装 + 単体テスト

## 概要
書籍の登録・更新APIを実装し、単体テストを作成する。

---

## 作業内容

### 1. ドメインモデル・DTO 作成

#### 入力

- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/domain/Book.kt`
  - `Book` データクラス（id, title, price, publicationStatus, authorIds）
  - `PublicationStatus` enum クラス（`UNPUBLISHED`, `PUBLISHED`）
- `src/main/kotlin/com/example/library_management/controller/dto/BookRequest.kt`
  - `BookRequest` データクラス（title, price, isPublished, authorIds）
- `src/main/kotlin/com/example/library_management/controller/dto/BookResponse.kt`
  - `BookResponse` データクラス（id, title, price, isPublished, authors）
  - `AuthorSummary` データクラス（id, name）— `BookResponse` 内にネストして定義、または同ファイルに定義

#### 備考

- `PublicationStatus` は `Book.kt` 内または同パッケージに定義する。DTO では `isPublished: Boolean` を使用し、サービス層でドメインモデルへの変換を行う
- バリデーションアノテーション（`@NotBlank`, `@Min(0)`, `@Size(min=1)` など）はリクエストDTOに付与する

---

### 2. BookRepository 実装

#### 入力

- `docs/spec/db_structure.md`（Task 1-1 の成果物）
- jOOQ 生成コード（Task 2-3 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/repository/BookRepository.kt`
  - `insert(book: Book): Book` — 書籍登録（`books` + `book_authors` への挿入）
  - `update(book: Book): Book` — 書籍更新（`books` 更新 + `book_authors` の洗い替え）
  - `findById(id: Long): Book?` — IDで書籍取得（著者IDリスト含む）

#### 備考

- `book_authors` の更新は既存レコードを DELETE してから INSERT する洗い替え方式とする
- jOOQ の生成クラス（`Tables.BOOKS`, `Tables.BOOK_AUTHORS`）を使ってクエリを記述する
- `DSLContext` を DI して使用する
- 存在しない ID への更新は `NotFoundException` をスローする

---

### 3. BookService 実装

#### 入力

- `BookRepository`（本タスク 4-2 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/service/BookService.kt`
  - `create(request: BookRequest): BookResponse` — 書籍登録
  - `update(id: Long, request: BookRequest): BookResponse` — 書籍更新

#### 備考

- バリデーション：`price` が 0 以上であること（DTOの `@Min(0)` と二重で確認）
- バリデーション：`authorIds` が1件以上であること
- バリデーション：`PUBLISHED` から `UNPUBLISHED` への変更は、更新前の状態を `findById` で取得して検証する
- バリデーション違反時は `IllegalArgumentException` をスローする
- `authorIds` に存在しない著者IDが含まれる場合は `NotFoundException` をスローする
- `BookRepository` および `AuthorRepository`（著者存在確認用）を DI して使用する

---

### 4. BookController 実装

#### 入力

- `BookService`（本タスク 4-3 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/controller/BookController.kt`
  - `POST /books` — 書籍登録
  - `PUT /books/{id}` — 書籍更新

#### 備考

- `@RestController` / `@RequestBody` / `@PathVariable` / `@Valid` を使用する
- 共通例外ハンドラー（`GlobalExceptionHandler`）は Task 3-4 で作成済みのものを流用する
- `POST /books` は 201 Created を返す

---

### 5. REST Client ファイル作成

#### 入力

- `BookController`（本タスク 4-4 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `docs/api/book.http`
  - `POST /books` — 書籍登録リクエスト例（正常系・各バリデーションエラー例）
  - `PUT /books/{id}` — 書籍更新リクエスト例（正常系・出版ステータス変更不可のエラー例）

#### 備考

- `@baseUrl = http://localhost:8080` の変数定義を先頭に置く
- `docs/api/author.http`（Task 3-5 の成果物）で著者を登録してから書籍登録を実行する手順をコメントで記載する

---

### 6. 単体テスト作成

#### 入力

- `BookService`（本タスク 4-3 の成果物）
- `BookController`（本タスク 4-4 の成果物）

#### 出力

- `src/test/kotlin/com/example/library_management/service/BookServiceTest.kt`
  - 正常系：書籍登録・更新が成功すること
  - 異常系：`price` が負の値の場合に例外がスローされること
  - 異常系：`authorIds` が空の場合に例外がスローされること
  - 異常系：`PUBLISHED` から `UNPUBLISHED` への変更で例外がスローされること
  - 異常系：存在しない ID への更新で例外がスローされること
- `src/test/kotlin/com/example/library_management/controller/BookControllerTest.kt`
  - 正常系：POST `/books` が 201 を返すこと
  - 正常系：PUT `/books/{id}` が 204 を返すこと
  - 異常系：各バリデーションエラー時に 400 を返すこと

#### 備考

- `BookServiceTest` は `@ExtendWith(MockKExtension::class)` を使用した純粋なユニットテストとする
- `BookControllerTest` は `@WebMvcTest(BookController::class)` を使用し MockMvc でHTTPレベルのテストを行う
- `BookService` のモックには `mockk<BookService>()` を使用する
