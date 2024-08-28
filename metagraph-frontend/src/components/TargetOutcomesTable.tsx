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
    const chartRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (chartRef.current) {
            // Clear any existing chart
            d3.select(chartRef.current).selectAll('*').remove();

            const data = targetOutcomes.map(d => d.stake);
            const maxStake = d3.max(data) || 0;

            const margin = { top: 30, right: 30, bottom: 0, left: 0 };
            const width = 400 - margin.left - margin.right;
            const rowHeight = 40; // Adjusted height for each row to match table row height
            const headerHeight = 40; // Height of the table header

            const x = d3.scaleLinear()
                .domain([0, maxStake])
                .range([0, width]);

            const svg = d3.select(chartRef.current)
                .append('svg')
                .attr('width', width + margin.left + margin.right)
                .attr('height', rowHeight * data.length + headerHeight + margin.top + margin.bottom)
                .append('g')
                .attr('transform', `translate(${margin.left},${margin.top})`);

            svg.append('g')
                .attr('transform', 'translate(0,0)')
                .call(d3.axisTop(x));

            svg.selectAll('.bar')
                .data(data)
                .enter()
                .append('rect')
                .attr('class', 'bar')
                .attr('x', 0)
                .attr('y', (d, i) => i * rowHeight + headerHeight)
                .attr('width', d => x(d))
                .attr('height', rowHeight - 10) // Adjusted height to fit within row
                .attr('fill', 'steelblue');
        }
    }, []);

    return (
        <Paper p="md" className="stakeholders-table">
            <Title order={3} mb="sm">Target Outcomes</Title>
            <Text mb="md">
                Target outcomes represent specific goals or objectives that are influenced by various exogenous variables.
                <br />
                Each target has a stake value indicating its importance or priority.
            </Text>
            <Grid>
                <Grid.Col span={6}>
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
                </Grid.Col>
                <Grid.Col span={6}>
                    <div ref={chartRef}></div>
                </Grid.Col>
            </Grid>
        </Paper>
    );
};