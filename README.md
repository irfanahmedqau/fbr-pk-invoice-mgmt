# FBR Invoice Management System

Full-stack application for channeling invoices to Pakistan's Federal Board of Revenue (FBR) via their sandbox and production APIs.

## Structure

```
fbr-pk-invoice-mgmt/
├── backend/    Spring Boot 3.x — REST API + FBR proxy + Excel parser
└── frontend/   Angular 17 + Angular Material — Invoice UI
```

## Features

- **Manual Invoice Entry** — enter single invoices with live FBR buyer validation (ATL check + registration type)
- **Bulk Excel Upload** — upload `.xlsx` files, auto-parse all rows, submit each invoice to FBR
- **Invoice Status Board** — track PENDING / VALIDATED / POSTED / FAILED status per invoice
- **Edit & Resubmit** — failed invoices can be corrected and resubmitted
- **Duplicate Prevention** — invoices already POSTED to FBR cannot be resubmitted
- **HS Code Autocomplete** — 7,800+ FBR HS codes searchable by code or description

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.9+ (or use `./mvnw`) |
| MySQL | 8.x |
| Node.js | 18+ |
| Angular CLI | 17+ |

## Backend Setup

```bash
cd backend

# 1. Create MySQL database
mysql -u root -p -e "CREATE DATABASE fbrdb;"

# 2. Configure credentials
#    Edit src/main/resources/application.properties
#    Replace YOUR_DB_USERNAME, YOUR_DB_PASSWORD, YOUR_FBR_API_TOKEN

# 3. Run
./mvnw spring-boot:run
# Starts on http://localhost:8081
```

## Frontend Setup

```bash
cd frontend

npm install
ng serve
# Opens on http://localhost:4200
```

## FBR API Configuration

All FBR endpoint URLs are in `backend/src/main/resources/application.properties`.  
To switch from **sandbox** to **production**, update `fbr.api.validate-url` — remove the `_sb` suffix and update any other environment-specific URLs.

## API Endpoints (Backend)

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/invoices/upload` | Validate + channel single invoice to FBR |
| `POST` | `/api/invoices/upload-excel` | Bulk Excel upload |
| `GET`  | `/api/invoices` | List all invoices with status |
| `GET`  | `/api/invoices/{id}` | Get invoice detail |
| `PUT`  | `/api/invoices/{id}/resubmit` | Resubmit a FAILED invoice |
| `POST` | `/api/invoices/save-invoice` | Save to DB only (no FBR call) |
| `GET`  | `/api/fbr/provinces` | FBR province list |
| `GET`  | `/api/fbr/uom` | FBR units of measure |
| `GET`  | `/api/fbr/hs-codes` | FBR HS codes (7,800+) |
| `GET`  | `/api/fbr/buyer-atl` | Check buyer ATL status |
| `GET`  | `/api/fbr/buyer-reg-type` | Get buyer registration type |
