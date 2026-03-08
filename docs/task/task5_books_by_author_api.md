# Task 5: 著者に紐づく書籍取得API実装 + 単体テスト

## 概要
著者IDに紐づく書籍一覧を返すAPIを実装し、単体テストを作成する。

---

## 作業内容

### 1. BookRepository に検索クエリ追加

#### 入力

- `docs/spec/db_structure.md`（Task 1-1 の成果物）
- jOOQ 生成コード（Task 2-3 の成果物）
- `BookRepository`（Task 4-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/repository/BookRepository.kt` に以下を追加する
  - `findByAuthorId(authorId: Long): List<Book>` — 著者IDに紐づく書籍一覧取得

#### 備考

- `books` と `book_authors` を JOIN して著者IDで絞り込むクエリを jOOQ で記述する
- 著者IDに紐づく書籍が0件の場合は空リストを返す

---

### 2. BookService に取得メソッド追加

#### 入力

- `BookRepository`（本タスク 5-1 の成果物）
- `AuthorRepository`（Task 3-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/service/BookService.kt` に以下を追加する
  - `findByAuthorId(authorId: Long): List<BookResponse>` — 著者IDに紐づく書籍一覧取得

#### 備考

- 指定した著者IDが存在しない場合は `NotFoundException` をスローする
- 著者は存在するが書籍が0件の場合は空リストを返す

---

### 3. AuthorController にエンドポイント追加

#### 入力

- `BookService`（本タスク 5-2 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `src/main/kotlin/com/example/library_management/controller/AuthorController.kt` に以下を追加する
  - `GET /authors/{id}/books` — 著者IDに紐づく書籍一覧取得

#### 備考

- `@PathVariable` で著者IDを受け取る
- `BookService` を DI して使用する
- 著者が存在しない場合は `GlobalExceptionHandler` が 404 Not Found を返す（Task 3-4 で定義した `NotFoundException` → 404 のマッピングが適用される）

---

### 4. REST Client ファイル作成

#### 入力

- `AuthorController`（本タスク 5-3 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）

#### 出力

- `docs/api/author.http`（Task 3-5 の成果物）に以下を追記する
  - `GET /authors/{id}/books` — 書籍一覧取得リクエスト例（正常系・著者が存在しない場合のエラー例）

#### 備考

- `docs/api/author.http` と `docs/api/book.http` を使って著者・書籍を登録した後に実行する手順をコメントで記載する

---

### 5. 単体テスト作成

#### 入力

- `BookService`（本タスク 5-2 の成果物）
- `AuthorController`（本タスク 5-3 の成果物）

#### 出力

- `src/test/kotlin/com/example/library_management/service/BookServiceTest.kt` に以下を追加する
  - 正常系：著者IDに紐づく書籍一覧が返ること
  - 正常系：著者は存在するが書籍が0件の場合に空リストが返ること
  - 異常系：存在しない著者IDを指定した場合に例外がスローされること
- `src/test/kotlin/com/example/library_management/controller/AuthorControllerTest.kt` に以下を追加する
  - 正常系：GET `/authors/{id}/books` が 200 と書籍一覧を返すこと
  - 異常系：存在しない著者IDを指定した場合に 404 を返すこと

#### 備考

- `BookServiceTest` は `@ExtendWith(MockKExtension::class)` を使用した純粋なユニットテストとする
- `AuthorControllerTest` は `@WebMvcTest` を使用し MockMvc でHTTPレベルのテストを行う
- `BookService` のモックには `mockk<BookService>()` を使用する
