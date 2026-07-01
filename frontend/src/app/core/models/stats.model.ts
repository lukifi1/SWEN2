export interface TourRank {
  name: string;
  popularity: number;
  childFriendliness: number;
}

export interface MonthlyDistance {
  month: string;
  distance: number;
}

export interface Stats {
  totalTours: number;
  totalLogs: number;
  totalTourDistance: number;
  totalLoggedDistance: number;
  totalLoggedTime: number;
  averageTourDistance: number;
  averageRating: number;
  difficultyDistribution: Record<string, number>;
  ratingDistribution: Record<string, number>;
  transportTypeBreakdown: Record<string, number>;
  popularityRanking: TourRank[];
  distanceOverTime: MonthlyDistance[];
}
