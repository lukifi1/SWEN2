/** Presentation helpers shared by the tour views (labels & formatting). */

export interface Label {
  text: string;
  badge: string;
}

export function childFriendlinessLabel(score: number | null | undefined): Label {
  const s = score ?? 0;
  if (s === 0)  return { text: 'Not rated yet', badge: 'badge' };
  if (s >= 66)  return { text: 'Child-friendly', badge: 'badge-green' };
  if (s >= 33)  return { text: 'Moderate', badge: 'badge-amber' };
  return { text: 'Challenging', badge: 'badge-red' };
}

export function popularityLabel(popularity: number | null | undefined): string {
  const p = popularity ?? 0;
  if (p === 0) return 'No logs yet';
  if (p >= 5) return 'Very popular';
  if (p >= 2) return 'Popular';
  return 'Rarely logged';
}

export function formatDistance(km: number | null | undefined): string {
  return km == null ? '–' : `${km.toFixed(1)} km`;
}

export function formatTime(hours: number | null | undefined): string {
  if (hours == null) return '–';
  const totalMinutes = Math.round(hours * 60);
  const h = Math.floor(totalMinutes / 60);
  const m = totalMinutes % 60;
  return h > 0 ? `${h} h ${m} min` : `${m} min`;
}
