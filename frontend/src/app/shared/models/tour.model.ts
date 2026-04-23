export interface TourLog {
  id: number;
  dateTime: string;
  comment: string;
  difficulty: number;
  totalDistance: number;
  totalTime: number;
  rating: number;
}

export interface Tour {
  id: number;
  name: string;
  description: string;
  from: string;
  to: string;
  transportType: string;
  distance: number;
  estimatedTime: number;
  image: string;
  logs: TourLog[];
  longitude: number;
  latitude: number;
}
