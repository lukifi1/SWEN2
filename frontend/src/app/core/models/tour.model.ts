/** A tour as returned by the backend (distance/time/route are computed via ORS). */
export interface Tour {
  id: number;
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: string;
  distance: number | null;
  estimatedTime: number | null;
  routeGeometry: string | null;
  imagePath: string | null;
  popularity: number;
  childFriendliness: number;
}

/** Payload for creating/updating a tour. */
export interface TourCreate {
  name: string;
  description: string;
  fromLocation: string;
  toLocation: string;
  transportType: string;
  imagePath?: string | null;
}

export interface TourLog {
  id: number;
  tourId: number;
  dateTime: string;
  comment: string;
  difficulty: number;
  totalDistance: number;
  totalTime: number;
  rating: number;
}

export interface TourLogCreate {
  dateTime: string;
  comment: string;
  difficulty: number;
  totalDistance: number;
  totalTime: number;
  rating: number;
}

export interface LocationSuggestion {
  label: string;
  longitude: number | null;
  latitude: number | null;
}
