import {
  childFriendlinessLabel,
  formatDistance,
  formatTime,
  popularityLabel,
} from './tour-format';

describe('tour-format helpers', () => {
  it('maps child-friendliness scores to display labels', () => {
    expect(childFriendlinessLabel(null)).toEqual({ text: 'Not rated yet', badge: 'badge' });
    expect(childFriendlinessLabel(80)).toEqual({ text: 'Child-friendly', badge: 'badge-green' });
    expect(childFriendlinessLabel(50)).toEqual({ text: 'Moderate', badge: 'badge-amber' });
    expect(childFriendlinessLabel(20)).toEqual({ text: 'Challenging', badge: 'badge-red' });
  });

  it('maps popularity values to display text', () => {
    expect(popularityLabel(null)).toBe('No logs yet');
    expect(popularityLabel(1)).toBe('Rarely logged');
    expect(popularityLabel(2)).toBe('Popular');
    expect(popularityLabel(5)).toBe('Very popular');
  });

  it('formats missing distance as a dash', () => {
    expect(formatDistance(null)).toBe('–');
  });

  it('formats distance with one decimal place', () => {
    expect(formatDistance(12.34)).toBe('12.3 km');
  });

  it('formats missing time as a dash', () => {
    expect(formatTime(undefined)).toBe('–');
  });

  it('formats time values in minutes or hours and minutes', () => {
    expect(formatTime(0.5)).toBe('30 min');
    expect(formatTime(1.5)).toBe('1 h 30 min');
  });
});
