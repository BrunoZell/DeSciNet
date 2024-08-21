export interface Surprise {
    latitudeSurprise: number;
    longitudeSurprise: number;
    timestamp: string;
    totalSurprise: number;
}

export interface SurpriseResponse {
    modelName: string;
    surprises: Surprise[];
    totalSurprise: number;
}
