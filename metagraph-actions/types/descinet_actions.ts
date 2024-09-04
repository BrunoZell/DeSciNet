type NewExternalVariableAction = {
    NewExternalVariable: {
      authority: string;
      uniqueName: string;
    };
  };
  
  type AdvanceMeasurementSequenceAction = {
    AdvanceMeasurementSequence: {
      externalVariableId: string;
      newHead: {
        externalVariableId: string;
        measurement: {
          timestamp: number;
          values: Record<string, number>;
        };
        previous: string | null;
      };
    };
  };
  
  type NewModelAction = {
    NewModel: {
      model: {
        author: string;
        externalParameterLabels: Record<string, string>;
        internalParameterLabels: Record<string, number>;
        internalVariables: Array<{
          equation: string;
        }>;
      };
    };
  };
  
  type NewSampleAction = {
    NewSample: {
      modelId: string;
      randomSeed: number;
      observationCutoff: Record<string, number>;
      delay: number;
      solution: Record<number, number>;
    };
  };
  
  type DeSciNetDataUpdateAction =
    | NewExternalVariableAction
    | AdvanceMeasurementSequenceAction
    | NewModelAction
    | NewSampleAction;
  
  export type {
    NewExternalVariableAction,
    AdvanceMeasurementSequenceAction,
    NewModelAction,
    NewSampleAction,
    DeSciNetDataUpdateAction
  };