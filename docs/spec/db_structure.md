# DB構造

## テーブル一覧

| テーブル名 | 論理名 | 概要 |
|---|---|---|
| author | 著者 | 著者の基本情報を管理するテーブル |
| book | 書籍 | 書籍の基本情報を管理するテーブル |
| book_author | 書籍著者 | 書籍と著者の多対多関係を管理する中間テーブル |

---

## テーブル定義

### author（著者）

| カラム名 | 型 | NULL | PK | 制約 | 説明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | ✓ | AUTO INCREMENT | 著者ID |
| name | VARCHAR(255) | NOT NULL | | | 著者名 |
| birth_date | DATE | NOT NULL | | 現在日以前であること | 生年月日 |

---

### book（書籍）

| カラム名 | 型 | NULL | PK | 制約 | 説明 |
|---|---|---|---|---|---|
| id | BIGINT | NOT NULL | ✓ | AUTO INCREMENT | 書籍ID |
| title | VARCHAR(255) | NOT NULL | | | タイトル |
| price | DECIMAL(10, 2) | NOT NULL | | 0以上であること | 価格 |
| is_published | BOOLEAN | NOT NULL | | デフォルト: FALSE | 出版済みフラグ（TRUE: 出版済み、FALSE: 未出版） |

> 備考: `is_published` が `TRUE` の書籍を `FALSE` に戻すことはアプリケーション層で禁止する。

---

### book_author（書籍著者）

| カラム名 | 型 | NULL | PK | 制約 | 説明 |
|---|---|---|---|---|---|
| book_id | BIGINT | NOT NULL | ✓ | FK: book.id | 書籍ID |
| author_id | BIGINT | NOT NULL | ✓ | FK: author.id | 著者ID |

---

## リレーション

- `book` と `author` は多対多の関係（中間テーブル `book_author` を介して結合）
- 1冊の書籍は最低1人の著者を持つ
- 1人の著者は複数の書籍を執筆できる

---

## ER図

```mermaid
erDiagram
    author {
        BIGINT id PK "著者ID"
        VARCHAR(255) name "著者名"
        DATE birth_date "生年月日"
    }

    book {
        BIGINT id PK "書籍ID"
        VARCHAR(255) title "タイトル"
        DECIMAL(10,2) price "価格（0以上）"
        BOOLEAN is_published "出版済みフラグ"
    }

    book_author {
        BIGINT book_id PK,FK "書籍ID"
        BIGINT author_id PK,FK "著者ID"
    }

    book ||--o{ book_author : "持つ"
    author ||--o{ book_author : "執筆する"
```
