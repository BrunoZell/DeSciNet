import React, { useEffect, useState, useRef, useCallback } from 'react';
import * as d3 from 'd3';
import { GoogleLocationMeasurement } from '../types/GoogleTimeline';
import { SurpriseResponse, Surprise } from '../types/SurpriseResponse';

const Page = () => {
    const svgRef = useRef(null);
    const [locationObservations, setLocationObservations] = useState<GoogleLocationMeasurement[]>([]);
    const [surpriseResponse, setSurpriseResponse] = useState<SurpriseResponse | null>(null);

    const handleDragOver = (event: React.DragEvent) => {
        event.preventDefault();
    };

    const transformHeader = (header: string) => {
        // Remove angle brackets from the header
        return header.replace(/<|>/g, '');
    };

    const handleDrop = (event: React.DragEvent) => {
        event.preventDefault();
        const files = event.dataTransfer.files;
        if (files.length) {
            const file = files[0];
            const reader = new FileReader();
            reader.onload = async (e) => {
                const text = e.target?.result as string;
                if (text.includes("timestamp")) {
                    parseGoogleLocationObservations(text);
                    await uploadFile(text);
                } else {
                    console.error("Unsupported file format.");
                }
            };
            reader.readAsText(file);
        }
    };

    const uploadFile = async (fileContent: string) => {
        try {
            console.log("Uploading file content:", fileContent); // Log file content
            const response = await fetch('http://localhost:5000/backtest/model-1', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: fileContent,
                timeout: 600000, // 10 minutes timeout
            });

            if (!response.ok) {
                throw new Error(`Server responded with status: ${response.status}`);
            }

            const rawText = await response.text();
            console.log("Raw response body:", rawText); // Log raw response body

            try {
                const parsedResponse = JSON.parse(rawText);
                console.log("Parsed response data:", parsedResponse);
                console.log("Type of parsedResponse:", typeof parsedResponse);

                // If parsedResponse is a string, parse it again
                if (typeof parsedResponse === 'string') {
                    const doubleParsedResponse = JSON.parse(parsedResponse);
                    console.log("Double parsed response data:", doubleParsedResponse);
                    console.log("Type of doubleParsedResponse:", typeof doubleParsedResponse);

                    // Check if the double parsed response is an object with a 'surprises' array
                    if (doubleParsedResponse && Array.isArray(doubleParsedResponse.surprises)) {
                        setSurpriseResponse(doubleParsedResponse); // Store the full response
                        console.log("Parsed response data:", doubleParsedResponse.surprises); // Log parsed response data
                    } else {
                        console.error("Unexpected response format:", doubleParsedResponse);
                    }
                } else {
                    console.error("Unexpected response format:", parsedResponse);
                }
            } catch (parseError) {
                console.error("Error parsing response JSON:", parseError);
                console.error("Response text:", rawText);
            }
        } catch (error) {
            console.error("Error uploading file:", error);
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

    useEffect(() => {
        if (surpriseResponse && surpriseResponse.surprises) {
            const surprises = surpriseResponse.surprises;
            console.log("Surprises to draw:", surpriseResponse);

            const svg = d3.select(svgRef.current);
            svg.selectAll("*").remove();

            const width = svgRef.current.clientWidth;
            const height = svgRef.current.clientHeight;

            const xScale = d3.scaleBand()
                .domain(surprises.map((d, i) => i.toString()))
                .range([50, width - 10])
                .padding(0.1);

            const yScale = d3.scaleLinear()
                .domain([0, d3.max(surprises, d => d.totalSurprise) || 0])
                .range([height - 30, 10]);

            // Plot bars
            svg.selectAll("rect")
                .data(surprises)
                .enter()
                .append("rect")
                .attr("x", (d, i) => xScale(i.toString())!)
                .attr("y", d => yScale(d.totalSurprise))
                .attr("width", xScale.bandwidth())
                .attr("height", d => height - 30 - yScale(d.totalSurprise))
                .attr("fill", "blue");

            // Axes
            const xAxis = svg.append("g")
                .attr("transform", `translate(0,${height - 30})`)
                .call(d3.axisBottom(xScale).tickFormat((d, i) => surprises[i].timestamp));

            const yAxis = svg.append("g")
                .attr("transform", `translate(50,0)`)
                .call(d3.axisLeft(yScale));
        }
    }, [surpriseResponse]);

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