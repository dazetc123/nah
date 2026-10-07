type Tone = 'ok' | 'neutral' | 'danger' | 'accent' | 'info' | 'warning' | 'critical'
export default function StatusPill({ tone, children }: { tone: Tone; children: string }) {
  return <span className={`pill pill-${tone}`}>{children}</span>
}
