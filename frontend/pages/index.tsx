import React, { useEffect, useState, useRef, useCallback } from 'react';
import * as d3 from 'd3';
import { GoogleLocationMeasurement } from '../types/GoogleTimeline';

// Utility function for processing data
const processDataForExtents = (datasets) => {
    let minDate = new Date(8640000000000000);
    let maxDate = new Date(-8640000000000000);
    let minValue = Infinity;
    let maxValue = -Infinity;

    datasets.forEach(data => {
        data.forEach(d => {
            if (d.DATETIME < minDate) minDate = d.DATETIME;
            if (d.DATETIME > maxDate) maxDate = d.DATETIME;
            if ('LOW' in d && d.LOW < minValue) minValue = d.LOW;
            if ('HIGH' in d && d.HIGH > maxValue) maxValue = d.HIGH;
            if ('BID' in d && d.BID < minValue) minValue = d.BID;
            if ('ASK' in d && d.ASK > maxValue) maxValue = d.ASK;
        });
    });

    return { minDate, maxDate, minValue, maxValue };
};

const Page = () => {
    const svgRef = useRef(null);
    const [locationObservations, setLocationObservations] = useState<GoogleLocationMeasurement[]>([]);

    const handleDragOver = (event: React.DragEvent) => {
        event.preventDefault();
    };

    const transformHeader = (header: string) => {
        // Remove angle brackets from the header
        return header.replace(/<|>/g, '');
    };

    const handleDrop = (event) => {
        event.preventDefault();
        const files = event.dataTransfer.files;
        if (files.length) {
            const file = files[0];
            const reader = new FileReader();
            reader.onload = (e) => {
                const text = e.target?.result as string;
                if (text.includes("timestamp")) {
                    parseGoogleLocationObservations(text);
                } else {
                    console.error("Unsupported file format.");
                }
            };
            reader.readAsText(file);
        }
    };

    const parseGoogleLocationObservations = (text: string) => {
        try {
            const data = JSON.parse(text);
            setLocationObservations(data);
        } catch (error) {
            console.error("Invalid JSON format.", error);
        }
    };

    const processDataForExtents = (data: GoogleLocationMeasurement[]) => {
            let minDate = new Date(data[0].timestamp);
            let maxDate = new Date(data[0].timestamp);
            for (let i = 1; i < data.length; i++) {
                const currentDate = new Date(data[i].timestamp);
                if (currentDate < minDate) minDate = currentDate;
                if (currentDate > maxDate) maxDate = currentDate;
            }

        return {
            minDate,
            maxDate
        };
    };

    const drawCandles = useCallback((data, intervalMinutes, className, scales) => {
        const { xScale, yScale } = scales;
        const svg = d3.select(svgRef.current);

        // Total chart time span in milliseconds
        let timeSpan = xScale.domain()[1] - xScale.domain()[0];

        // Calculate total width of the chart in pixels
        let totalWidth = xScale.range()[1] - xScale.range()[0];

    }, []);

    useEffect(() => {
        if (locationObservations.length > 0) {
            const svg = d3.select(svgRef.current);
            svg.selectAll("*").remove();

            const width = svgRef.current.clientWidth;
            const height = svgRef.current.clientHeight;

            const xScale = d3.scaleLinear()
                .domain(d3.extent(locationObservations, d => d.longitude))
                .range([50, width - 10]);

            const yScale = d3.scaleLinear()
                .domain(d3.extent(locationObservations, d => d.latitude))
                .range([height - 30, 10]);

            // Plot points
            svg.selectAll("circle")
                .data(locationObservations)
                .enter()
                .append("circle")
                .attr("cx", d => xScale(d.longitude))
                .attr("cy", d => yScale(d.latitude))
                .attr("r", 3)
                .attr("fill", "blue");

            // Axes
            const xAxis = svg.append("g")
                .attr("transform", `translate(0,${height - 30})`)
                .call(d3.axisBottom(xScale));

            const yAxis = svg.append("g")
                .attr("transform", `translate(50,0)`)
                .call(d3.axisLeft(yScale));
        }
    }, [locationObservations]);

    return (
        <div style={{ width: '100vw', height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            <svg ref={svgRef} onDragOver={handleDragOver} onDrop={handleDrop} style={{ width: '100%', height: '100%' }}>
                {locationObservations.length === 0 && (
                    <text x="50%" y="50%" textAnchor="middle" fill="gray">Drag and drop your observations here</text>
                )}
            </svg>
        </div>
    );
};

export default Page;