import type { ReactNode } from 'react'
import { Activity, Moon, Sun } from 'lucide-react'
import { useTheme } from '../../lib/theme'

export default function AuthLayout({ eyebrow, headline, blurb, children }: {
  eyebrow: string; headline: ReactNode; blurb: string; children: ReactNode
}) {
  const { theme, toggle } = useTheme()
  return (
    <div className="auth-page">
      <aside className="auth-aside">
        <div className="brand"><span className="brand-mark"><Activity size={20} /></span><strong>Betong<span>Ops</span></strong></div>
        <div>
          <p className="eyebrow auth-eyebrow" style={{ margin: 0 }}>{eyebrow}</p>
          <h2>{headline}</h2>
          <p>{blurb}</p>
        </div>
        <small className="auth-copyright">© 2026 BetongOps · Nền tảng quản lý bê tông thương phẩm</small>
      </aside>
      <main className="auth-main">
        <button className="icon-btn auth-theme" onClick={toggle} aria-label="Đổi giao diện sáng/tối" title={theme === 'dark' ? 'Nền sáng' : 'Nền tối'}>
          {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>
        <div className="auth-card">{children}</div>
      </main>
    </div>
  )
}
