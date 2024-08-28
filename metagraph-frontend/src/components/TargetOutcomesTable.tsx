import { useEffect, useRef, useState } from 'react';
import { Paper, Table, Tooltip, Grid, Title, Text } from '@mantine/core';
import * as d3 from 'd3';

// Define the TypeScript type for the stakeholder data
type TargetOutcome = {
    name: string;
    stake: number;
    exogenousVariables: string[];
};

// Define the constant with the stakeholder data
const targetOutcomes: TargetOutcome[] = [
    { name: 'Target 1', stake: 16500, exogenousVariables: ['x', 'y'] },
    { name: 'Target 2', stake: 4000, exogenousVariables: ['a', 'b'] },
    { name: 'Target 3', stake: 4000, exogenousVariables: ['c', 'd'] }
];

export const TargetOutcomesTable = () => {
    return (
        <Paper p="md" className="stakeholders-table">
            <Title order={3} mb="sm">Target Outcomes</Title>
            <Text mb="md">
                Target outcomes represent specific goals or objectives that are influenced by various exogenous variables.
                <br />
                Each target has a stake value indicating its importance or priority.
            </Text>
            <Table>
                <thead>
                    <tr>
                        <th>Model</th>
                        <th>Stake</th>
                    </tr>
                </thead>
                <tbody>
                    {targetOutcomes.map((targetOutcome, index) => (
                        <tr key={index}>
                            <td>{targetOutcome.name}</td>
                            <td>{targetOutcome.stake.toLocaleString()}</td>
                        </tr>
                    ))}
                </tbody>
            </Table>
        </Paper>
    );
};