import { createContext, useContext, useLayoutEffect, useState, type ReactNode } from 'react'

type Theme = 'light' | 'dark'
const ThemeContext = createContext<{ theme: Theme; toggle: () => void } | null>(null)

function applyTheme(theme: Theme) {
  const root = document.documentElement
  root.dataset.theme = theme
  root.classList.toggle('theme-light', theme === 'light')
  root.classList.toggle('theme-dark', theme === 'dark')
  root.style.colorScheme = theme
  
  // Xoá các style inline cũ bị set cứng để nhường lại quyền điều khiển cho theme.css
  root.style.removeProperty('--bg')
  root.style.removeProperty('--panel')
  root.style.removeProperty('--sidebar-bg')
  document.body.style.removeProperty('background-color')
  document.getElementById('root')?.style.removeProperty('background-color')
}

function initialTheme(): Theme {
  const saved = localStorage.getItem('betong_theme_pref') as Theme | null
  if (saved === 'light' || saved === 'dark') return saved
  
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark'
  }
  return 'light'
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>(initialTheme)
  
  useLayoutEffect(() => {
    applyTheme(theme)
    localStorage.setItem('betong_theme_pref', theme)
  }, [theme])
  
  return (
    <ThemeContext.Provider value={{ theme, toggle: () => setTheme((t) => (t === 'dark' ? 'light' : 'dark')) }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTheme() {
  const ctx = useContext(ThemeContext)
  if (!ctx) throw new Error('useTheme phải nằm trong <ThemeProvider>')
  return ctx
}
