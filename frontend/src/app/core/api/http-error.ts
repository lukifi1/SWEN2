import { HttpErrorResponse } from '@angular/common/http';

/** Extracts a user-friendly message from the backend's ErrorResponse body. */
export function extractMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error;
    if (body && typeof body === 'object' && typeof body.message === 'string') {
      return body.message;
    }
    if (error.status === 0) {
      return 'Cannot reach the server. Is the backend running?';
    }
  }
  return fallback;
}
