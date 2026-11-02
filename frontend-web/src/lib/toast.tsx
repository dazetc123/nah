import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { Check, CircleAlert } from 'lucide-react'

interface Item { id: number; text: string; error: boolean }
interface ToastApi { success: (text: string) => void; error: (text: string) => void }

const ToastContext = createContext<ToastApi | null>(null)
let seq = 0

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Item[]>([])
  const push = useCallback((text: string, error: boolean) => {
    const id = ++seq
    setItems((prev) => [...prev, { id, text, error }])
    window.setTimeout(() => setItems((prev) => prev.filter((i) => i.id !== id)), error ? 5000 : 3500)
  }, [])
  const api: ToastApi = { success: (t) => push(t, false), error: (t) => push(t, true) }
  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="toast-stack" role="status" aria-live="polite">
        {items.map((i) => (
          <div key={i.id} className={i.error ? 'toast toast-error' : 'toast'}>
            {i.error ? <CircleAlert size={16} /> : <Check size={16} />}
            <span>{i.text}</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast() {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast phải nằm trong <ToastProvider>')
  return ctx
}
