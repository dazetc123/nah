import { Menu, Moon, Sun } from 'lucide-react'
import { useTheme } from '../../lib/theme'

export default function Topbar({ title, onMenu }: { title: string; onMenu: () => void }) {
  const { theme, toggle } = useTheme()
  return (
    <header className="topbar">
      <button className="icon-btn mobile-menu" onClick={onMenu} aria-label="Mở menu"><Menu size={20} /></button>
      <div className="crumb">Trang chủ<span>/</span><b>{title}</b></div>
      <div className="top-actions">
        <button className="btn btn-sm" onClick={toggle} title="Bấm để đổi giao diện sáng/tối">
          {theme === 'dark' ? <Sun size={15} /> : <Moon size={15} />}
          {theme === 'dark' ? 'Đổi sang nền sáng' : 'Đổi sang nền tối'}
        </button>
      </div>
    </header>
  )
}
