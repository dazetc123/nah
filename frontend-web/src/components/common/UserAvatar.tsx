import { useEffect, useState } from 'react'
import { API_URL } from '../../lib/api/client'

const initials = (name: string) => name.split(' ').filter(Boolean).map((part) => part[0]).slice(-2).join('').toUpperCase() || 'U'

function imageUrl(value?: string | null) {
  if (!value) return ''
  if (/^(https?:|data:|blob:)/i.test(value)) return value
  return `${API_URL.replace(/\/$/, '')}/${value.replace(/^\//, '')}`
}

function storedAvatar(src?: string | null, preferStored = false) {
  return preferStored
    ? localStorage.getItem('betong_avatar_url') || src || ''
    : src || ''
}

export default function UserAvatar({ name, src, className = '', preferStored = false }: {
  name: string; src?: string | null; className?: string; preferStored?: boolean
}) {
  const [avatar, setAvatar] = useState(() => storedAvatar(src, preferStored))
  useEffect(() => {
    setAvatar(storedAvatar(src, preferStored))
    const refresh = () => setAvatar(storedAvatar(src, preferStored))
    window.addEventListener('betong-avatar-updated', refresh)
    return () => window.removeEventListener('betong-avatar-updated', refresh)
  }, [src, preferStored])
  const url = imageUrl(avatar)
  return url
    ? <img key={url} className={`avatar avatar-image ${className}`} src={url} alt={`Ảnh đại diện của ${name}`} onError={() => setAvatar(storedAvatar(src))} />
    : <span className={`avatar ${className}`} aria-label={`Ảnh đại diện của ${name}`}>{initials(name)}</span>
}
