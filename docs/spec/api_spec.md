# API エンドポイント仕様

---

## 共通仕様

- Content-Type: `application/json`
- 文字コード: UTF-8
- 日付フォーマット: `YYYY-MM-DD`（例: `2000-01-23`）
- 日時フォーマット: ISO 8601（例: `2026-03-08T12:00:00Z`）
- 登録系（POST）: 成功時は `201 Created`、レスポンスボディは `id` のみ返す
- 更新系（PUT）: 成功時は `204 No Content`、レスポンスボディなし
- 取得系（GET）: 成功時は `200 OK`

---

## 1. 著者登録

### メソッド・パス

```
POST /authors
```

### パスパラメータ

なし

### クエリパラメータ

なし

### リクエストボディ

| フィールド名 | 型 | 必須/任意 | バリデーション条件 |
|---|---|---|---|
| name | String | 必須 | 空文字不可 |
| birthDate | String (date) | 必須 | 現在日以前であること (`YYYY-MM-DD`) |

```json
{
  "name": "著者名",
  "birthDate": "1990-04-01"
}
```

### レスポンスボディ（201 Created）

| フィールド名 | 型 | 説明 |
|---|---|---|
| id | Long | 著者ID |

```json
{
  "id": 1
}
```

### エラーレスポンス

| ステータスコード | 発生条件 |
|---|---|
| 400 Bad Request | リクエストボディのバリデーションエラー（必須項目の欠如、birthDate が未来日など） |

---

## 2. 著者更新

### メソッド・パス

```
PUT /authors/{authorId}
```

### パスパラメータ

| 名前 | 型 | 説明 |
|---|---|---|
| authorId | Long | 更新対象の著者ID |

### クエリパラメータ

なし

### リクエストボディ

| フィールド名 | 型 | 必須/任意 | バリデーション条件 |
|---|---|---|---|
| name | String | 必須 | 空文字不可 |
| birthDate | String (date) | 必須 | 現在日以前であること (`YYYY-MM-DD`) |

```json
{
  "name": "新しい著者名",
  "birthDate": "1985-07-15"
}
```

### レスポンス

`204 No Content`（レスポンスボディなし）

### エラーレスポンス

| ステータスコード | 発生条件 |
|---|---|
| 400 Bad Request | リクエストボディのバリデーションエラー（必須項目の欠如、birthDate が未来日など） |
| 404 Not Found | 指定された authorId の著者が存在しない |

---

## 3. 書籍登録

### メソッド・パス

```
POST /books
```

### パスパラメータ

なし

### クエリパラメータ

なし

### リクエストボディ

| フィールド名 | 型 | 必須/任意 | バリデーション条件 |
|---|---|---|---|
| title | String | 必須 | 空文字不可 |
| price | Int | 任意 | 値が指定された場合は0以上 |
| published | Boolean | 必須 | `true`（出版済み）または `false`（未出版） |
| authorIds | List\<Long\> | 必須 | 1件以上。存在する著者IDであること |

```json
{
  "title": "書籍タイトル",
  "price": 1500,
  "published": false,
  "authorIds": [1, 2]
}
```

### レスポンスボディ（201 Created）

| フィールド名 | 型 | 説明 |
|---|---|---|
| id | Long | 書籍ID |

```json
{
  "id": 1
}
```

### エラーレスポンス

| ステータスコード | 発生条件 |
|---|---|
| 400 Bad Request | リクエストボディのバリデーションエラー（price が指定された場合に負、authorIds が空、published が null など） |
| 404 Not Found | authorIds に存在しない著者IDが含まれる |

---

## 4. 書籍更新

### メソッド・パス

```
PUT /books/{bookId}
```

### パスパラメータ

| 名前 | 型 | 説明 |
|---|---|---|
| bookId | Long | 更新対象の書籍ID |

### クエリパラメータ

なし

### リクエストボディ

| フィールド名 | 型 | 必須/任意 | バリデーション条件 |
|---|---|---|---|
| title | String | 必須 | 空文字不可 |
| price | Int | 任意 | 値が指定された場合は0以上 |
| authorIds | List\<Long\> | 必須 | 1件以上。存在する著者IDであること |
| published | Boolean | 必須 | `true`（出版済み）または `false`（未出版）。`true` から `false` への変更は不可 |

```json
{
  "title": "新しいタイトル",
  "price": 2000,
  "authorIds": [1],
  "published": true
}
```

### レスポンス

`204 No Content`（レスポンスボディなし）


### エラーレスポンス

| ステータスコード | 発生条件 |
|---|---|
| 400 Bad Request | リクエストボディのバリデーションエラー（price が指定された場合に負、authorIds が空、isPublished: true → false への変更など） |
| 404 Not Found | 指定された bookId の書籍が存在しない、または authorIds に存在しない著者IDが含まれる |

---

## 5. 著者に紐づく書籍一覧取得

### メソッド・パス

```
GET /authors/{authorId}/books
```

### パスパラメータ

| 名前 | 型 | 説明 |
|---|---|---|
| authorId | Long | 著者ID |

### クエリパラメータ

なし

### リクエストボディ

なし

### レスポンスボディ（200 OK）

書籍オブジェクトの配列（書籍IDの昇順で返す）

| フィールド名 | 型 | 説明 |
|---|---|---|
| id | Long | 書籍ID |
| title | String | タイトル |
| price | Int \| null | 価格（未設定の場合は null） |
| published | Boolean | 出版済みフラグ（`true`: 出版済み、`false`: 未出版） |
| authors | List\<Author\> | 著者一覧 |

**Author オブジェクト**

| フィールド名 | 型 | 説明 |
|---|---|---|
| id | Long | 著者ID |
| name | String | 著者名 |

```json
[
  {
    "id": 1,
    "title": "書籍タイトルA",
    "price": 1500,
    "published": true,
    "authors": [
      { "id": 1, "name": "著者名A" }
    ]
  },
  {
    "id": 2,
    "title": "書籍タイトルB",
    "price": 800,
    "published": false,
    "authors": [
      { "id": 1, "name": "著者名A" },
      { "id": 2, "name": "著者名B" }
    ]
  }
]
```

### エラーレスポンス

| ステータスコード | 発生条件 |
|---|---|
| 404 Not Found | 指定された authorId の著者が存在しない |
