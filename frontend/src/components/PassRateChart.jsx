import React, { useRef, useEffect } from 'react'
import * as echarts from 'echarts'

export default function PassRateChart({ data }) {
  const chartRef = useRef(null)
  let chartInstance = null

  useEffect(() => {
    if (chartRef.current) {
      chartInstance = echarts.init(chartRef.current)
      const option = {
        title: { text: '规则通过率' },
        tooltip: { 
          trigger: 'item',
          formatter: (params) => {
            return `${params.name}: ${(params.value * 100).toFixed(2)}%`
          }
        },
        xAxis: { type: 'category', data: data.map(item => item.ruleName) },
        yAxis: { 
          type: 'value', 
          max: 1,
          axisLabel: {
            formatter: (value) => (value * 100) + '%'
          }
        },
        series: [
          { 
            name: '通过率', 
            type: 'bar', 
            data: data.map(item => item.passRate),
            itemStyle: {
              color: (params) => {
                const rate = params.data
                if (rate >= 0.9) return '#52c41a'
                if (rate >= 0.5) return '#faad14'
                return '#f5222d'
              }
            }
          }
        ],
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
