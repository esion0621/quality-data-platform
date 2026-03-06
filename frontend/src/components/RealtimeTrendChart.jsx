import React, { useRef, useEffect } from 'react'
import * as echarts from 'echarts'

export default function RealtimeTrendChart({ data }) {
  const chartRef = useRef(null)
  let chartInstance = null

  useEffect(() => {
    if (chartRef.current) {
      chartInstance = echarts.init(chartRef.current)
      const series = Object.keys(data).map(ruleId => ({
        name: `规则${ruleId}`,
        type: 'line',
        data: data[ruleId]?.map(point => point.rate) || [],
      }))
      const xAxisData = data[Object.keys(data)[0]]?.map(point => 
        new Date(point.windowEnd).toLocaleTimeString()
      ) || []

      const option = {
        title: { text: '实时异常率趋势' },
        tooltip: { trigger: 'axis' },
        legend: { data: Object.keys(data).map(id => `规则${id}`) },
        xAxis: { type: 'category', data: xAxisData },
        yAxis: { 
          type: 'value', 
          max: 1,
          axisLabel: {
            formatter: (value) => (value * 100) + '%'
          }
        },
        series,
      }
      chartInstance.setOption(option)
    }

    return () => {
      if (chartInstance) {
        chartInstance.dispose()
      }
    }
  }, [data])

  return <div ref={chartRef} style={{ width: '100%', height: 400 }}></div>
}
