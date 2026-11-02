import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'

export default function PasswordInput({ value, onChange, autoComplete, placeholder, required = true }: {
  value: string; onChange: (v: string) => void; autoComplete?: string; placeholder?: string; required?: boolean
}) {
  const [show, setShow] = useState(false)
  return (
    <div className="password-input">
      <input type={show ? 'text' : 'password'} value={value} onChange={(e) => onChange(e.target.value)}
        autoComplete={autoComplete} placeholder={placeholder} required={required} />
      <button type="button" onClick={() => setShow(!show)} aria-label={show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}>
        {show ? <EyeOff size={17} /> : <Eye size={17} />}
      </button>
    </div>
  )
}
