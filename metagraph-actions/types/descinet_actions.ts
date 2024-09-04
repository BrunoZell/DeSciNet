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
  
/* ####### Original Scala Type #######
  @derive(decoder, encoder)
  case class Model(
    /// Implicitly this model defines:
    /// I : index-set of endogenous variables (=all equations over all possible parameters)
    /// J : index-set of exogenous variables, as a subset of the global J made of all ExternalVariables of the DeSciNet Metagraph
    /// PA_x(i) : { j in J } for each i in I : direct exogenous parents of the i-th endogenous variable
    /// PA_y(i) : { i in I }for each i in I : direct endogenous parents of the i-th endogenous variable
    author                  : Address,
    externalParameterLabels : Map[String, String], // 'Key: equation-label; 'Value: Hash[ExternalVariable]
    internalParameterLabels : Map[String, Int], // 'Key: equation-label; 'Value: position index in this.internalVariables
    internalVariables       : List[InternalVariable],
  )
*/
  type NewModelAction = {
    NewModel: {
      model: {
        author: string;
        externalParameterLabels: Record<string, string>; // 'Key: equation-label; 'Value: Hash[ExternalVariable]
        internalParameterLabels: Record<string, number>; // 'Key: equation-label; 'Value: position index in this.internalVariables
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