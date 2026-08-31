# Smart Internship Tracker Web

React/Vite frontend for Smart Internship Tracker.

The deployed app is available at:

```text
https://smart-internship-tracker-ebon.vercel.app
```

## Local Development

Install dependencies:

```bash
npm install
```

Create a local environment file:

```bash
cp .env.example .env
```

Run the Vite dev server:

```bash
npm run dev
```

The frontend reads its API base URL from:

```text
VITE_API_URL
```

Local default:

```text
http://localhost:8080/api
```

Current deployed backend:

```text
https://3-21-242-207.sslip.io/api
```

Do not put secrets in `VITE_*` variables. Vite bundles these values into browser JavaScript.

## Verification

Run tests:

```bash
npm run test
```

Run TypeScript checks:

```bash
npm run typecheck
```

Build production assets:

```bash
npm run build
```

## Vercel Settings

Vercel imports this app from the monorepo using:

```text
Root Directory: apps/web
Framework Preset: Vite
Build Command: npm run build
Output Directory: dist
Install Command: npm install
```

Production environment variable:

```text
VITE_API_URL=https://3-21-242-207.sslip.io/api
```

The Spring Boot API lives in `apps/api`.
