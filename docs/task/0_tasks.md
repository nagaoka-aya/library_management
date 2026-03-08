# タスク一覧

## Task 1: DB設計
- `authors`、`books`、`book_authors` の3テーブルを設計し、カラム定義・制約・リレーションをドキュメント化する
- APIエンドポイント一覧（登録・更新・取得）を定義する

## Task 2: 環境構築
- `pom.xml` に Flyway 依存を追加する
- DDLマイグレーションスクリプト（`V1__create_tables.sql`）を作成する
- jOOQ コード生成プラグインを設定し、コードを生成する
- `application.properties` の H2 DB 接続設定を確認する

## Task 3: 著者登録・更新API実装 + 単体テスト
- `Author` ドメインモデルおよび Request/Response DTO を作成する
- `AuthorRepository`（jOOQ）・`AuthorService`・`AuthorController` を実装する
- バリデーション：生年月日が現在日以前であること
- 単体テスト：正常系・バリデーションエラーの異常系を実装する

## Task 4: 書籍登録・更新API実装 + 単体テスト
- `Book` ドメインモデルおよび Request/Response DTO を作成する
- `BookRepository`（jOOQ）・`BookService`・`BookController` を実装する
- バリデーション：価格 >= 0、著者 >= 1、出版済みから未出版への変更不可
- 単体テスト：正常系・各バリデーションエラー・状態遷移制約の異常系を実装する

## Task 5: 著者に紐づく書籍取得API実装 + 単体テスト
- 著者IDで書籍一覧を返すエンドポイントを実装する
- `BookRepository` に著者IDでの検索クエリを追加する（jOOQ JOIN）
- 単体テスト：正常系・著者が存在しない場合の異常系を実装する
