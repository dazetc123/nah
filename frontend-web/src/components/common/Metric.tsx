import type { ReactNode } from 'react'

export default function Metric({ label, value, delta, icon, tone }: { label: string; value: string; delta: string; icon: ReactNode; tone: string }) {
  return <div className="metric-card"><div className={`metric-icon ${tone}`}>{icon}</div><div><p>{label}</p><strong>{value}</strong><small className={delta.includes('%') && !delta.includes('đội') ? 'positive' : ''}>{delta}</small></div></div>
}
