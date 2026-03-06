import React, { useRef, useEffect } from 'react'
import * as echarts from 'echarts'

export default function TrendChart({ data }) {
  const chartRef = useRef(null)
  let chartInstance = null

  useEffect(() => {
    if (chartRef.current) {
      chartInstance = echarts.init(chartRef.current)
      const option = {
        title: { text: '最近7天任务趋势' },
        tooltip: { trigger: 'axis' },
        legend: { data: ['任务总数', '成功数', '失败数'] },
        xAxis: { type: 'category', data: data.map(item => item.date) },
        yAxis: { type: 'value' },
        series: [
          { name: '任务总数', type: 'line', data: data.map(item => item.totalCount) },
          { name: '成功数', type: 'line', data: data.map(item => item.successCount) },
          { name: '失败数', type: 'line', data: data.map(item => item.failCount) },
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
