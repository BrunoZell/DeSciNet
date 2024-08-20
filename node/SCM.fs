module SCM

type Variable<'a> = {
    Name: string
    Value: 'a
}

type Equation<'a> = {
    Variables: Variable<'a> list
    Calculate: unit -> 'a
}

type Observation<'a> = {
    Variables: Variable<'a> list
    Timestamp: Timestamp
}

type Intervention<'a> = {
    Name: string
    Apply: unit -> Equation<'a> list
}

type CausalModel<'a> = {
    Variables: Variable<'a> list
    Equations: Equation<'a> list
    Interventions: Intervention<'a> list
}

type World<'a> = {
    CausalModel: CausalModel<'a>
    Timestamp: Timestamp
    History: World<'a> list
}

let abduction (world: World<'a>) (observation: Variable<'a>) : Intervention<'a> option =
    // Logic to infer the most likely intervention that caused the observation

let quantifySurprise (world: World<'a>) (observation: Variable<'a>) : float =
    // Logic to quantify the surprise of an observation given the current world state