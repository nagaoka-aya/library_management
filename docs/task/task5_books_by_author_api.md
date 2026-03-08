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
- `docs/spec/implementation_rules.md`（実装ルール）

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
- `docs/spec/implementation_rules.md`（実装ルール）

#### 出力

- `src/main/kotlin/com/example/library_management/service/BookService.kt` に以下を追加する
  - `findByAuthorId(authorId: Long): List<BookResponse>` — 著者IDに紐づく書籍一覧取得

#### 備考

- 著者は存在するが書籍が0件の場合は空リストを返す

---

### 3. AuthorController にエンドポイント追加

#### 入力

- `BookService`（本タスク 5-2 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）
- `docs/spec/implementation_rules.md`（実装ルール）

#### 出力

- `src/main/kotlin/com/example/library_management/controller/AuthorController.kt` に以下を追加する
  - `GET /authors/{id}/books` — 著者IDに紐づく書籍一覧取得

#### 備考

- `@PathVariable` で著者IDを受け取る
- `BookService` を DI して使用する
- 著者が存在しない場合は `BookService.findByAuthorId` が `null` を返すため、Controller で `NotFoundException` をスローする（`GlobalExceptionHandler` が 404 Not Found にマッピングする）

---

### 4. REST Client ファイル作成

#### 入力

- `AuthorController`（本タスク 5-3 の成果物）
- `docs/spec/api_spec.md`（Task 1-2 の成果物）
- `docs/spec/implementation_rules.md`（実装ルール）

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
- `docs/spec/implementation_rules.md`（実装ルール）

#### 出力

- `src/test/kotlin/com/example/library_management/service/BookServiceTest.kt` に以下を追加する
  - 著者IDに紐づく書籍一覧取得：正常系（著者IDに紐づく書籍が複数件返ること）
  - 著者IDに紐づく書籍一覧取得：正常系（著者は存在するが書籍が0件の場合に空リストが返ること）
  - 著者IDに紐づく書籍一覧取得：正常系（複数著者に紐づく書籍で、対象著者の書籍のみ返ること）
  - 著者IDに紐づく書籍一覧取得：異常系（存在しない著者IDを指定した場合に null が返ること）
- `src/test/kotlin/com/example/library_management/controller/AuthorControllerTest.kt` に以下を追加する
  - 著者IDに紐づく書籍一覧取得：正常系（200 と1件の書籍 JSON が返ること）
  - 著者IDに紐づく書籍一覧取得：正常系（200 と３件の書籍一覧 JSON が返ること）
  - 著者IDに紐づく書籍一覧取得：正常系（書籍が0件の場合に 200 と空配列が返ること）
  - 著者IDに紐づく書籍一覧取得：異常系（存在しない著者IDを指定した場合に 404 が返ること）

#### 備考

- `BookServiceTest` は `@SpringBootTest` + `@Transactional` を使用した統合テストとする（実際の DB に対して検証し、テスト後はロールバックされる）
- Service と Repository は DI で注入し、モックは使用しない
- `AuthorControllerTest` は `@WebMvcTest` を使用し MockMvc でHTTPレベルのテストを行う
- `BookService` のモックには `@MockkBean` を使用する
