# Budget Tracker ERD

```mermaid

erDiagram
    USER ||--o| PAYCHECK : has_active
    PAYCHECK ||--o{ FINANCIALS : allocates
    CATEGORY ||--o{ FINANCIALS : has_amount_for
    FINANCIALS ||--o{ EXPENSE : tracks
    USER ||--o{ AUTH_ACCOUNT : signs_in_with
    USER o|--o{ CATEGORY : creates

    USER {
        int user_id PK
        string email
        string display_name
        string role
    }

    AUTH_ACCOUNT {
        int auth_account_id PK
        int user_id FK
        string provider
        string provider_user_id
    }

    PAYCHECK {
        int paycheck_id PK
        int user_id FK,UK
        decimal paycheck_amount
        decimal savings_percent
    }

    CATEGORY {
        int category_id PK
        int created_by_user_id FK
        string name
        boolean is_active
    }

    FINANCIALS {
        int financial_id PK
        int paycheck_id FK
        int category_id FK
        decimal set_amount
    }

    EXPENSE {
        int expense_id PK
        int financial_id FK
        decimal amount
        string description
    }

```
