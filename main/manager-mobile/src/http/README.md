# HTTP client

This project uses Alova as its only HTTP request library:

## Usage

- **Alova HTTP**: path `src/http/request/alova.ts`
- **Example code**: `src/api/foo-alova.ts` and `src/api/foo.ts`
- **API documentation**: https://alova.js.org/

## Configuration

The Alova instance is configured with:
- Automatic token auth and refresh
- Unified error handling and toasts
- Dynamic domain switching
- Built-in request/response interceptors

## Example

```typescript
import { http } from '@/http/request/alova'

// GET request
http.Get<ResponseType>('/api/path', {
  params: { id: 1 },
  headers: { 'Custom-Header': 'value' },
  meta: { toast: false } // disable error toast
})

// POST request
http.Post<ResponseType>('/api/path', data, {
  params: { query: 'param' },
  headers: { 'Content-Type': 'application/json' }
})
```
