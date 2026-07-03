import { HttpErrorResponse } from '@angular/common/http';
import { extractMessage } from './http-error';

describe('extractMessage', () => {
  it('uses backend error response messages when present', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { message: 'Name must not be blank.' },
    });

    expect(extractMessage(error, 'Fallback')).toBe('Name must not be blank.');
  });

  it('returns a connectivity message for network errors', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(extractMessage(error, 'Fallback')).toBe('Cannot reach the server. Is the backend running?');
  });

  it('falls back for unknown error shapes', () => {
    expect(extractMessage(new Error('boom'), 'Fallback')).toBe('Fallback');
  });
});
