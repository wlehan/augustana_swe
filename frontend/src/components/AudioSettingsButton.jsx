import { useEffect, useRef, useState } from 'react'
import { useAudio } from '../audio/AudioContext'
import './AudioSettingsButton.css'

/**
 * Reusable settings menu for music/sound plus optional page-specific actions.
 */
function AudioSettingsButton({ iconSrc, iconAlt, className = '', menuAlign = 'left', children }) {
  const [isOpen, setIsOpen] = useState(false)
  const menuRef = useRef(null)
  const { isSoundEnabled, setIsSoundEnabled } = useAudio()

  useEffect(() => {
    if (!isOpen) {
      return undefined
    }

    const handlePointerDown = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsOpen(false)
      }
    }

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    document.addEventListener('keydown', handleEscape)

    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [isOpen])

  return (
    <div className={`audio-settings-wrap ${className}`.trim()} ref={menuRef}>
      <button
        type="button"
        className="icon-btn audio-settings-trigger"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-label="Open settings"
        aria-expanded={isOpen}
      >
        <img src={iconSrc} className="nav-image" alt={iconAlt} />
      </button>

      {isOpen && (
        <div className={`settings-menu settings-menu-${menuAlign}`}>
          <p className="settings-title">Settings</p>
          <button
            className="settings-item"
            type="button"
            onClick={() => setIsSoundEnabled(!isSoundEnabled)}
          >
            <span>Music and sound</span>
            <span className={`toggle-pill ${isSoundEnabled ? 'on' : ''}`}>
              {isSoundEnabled ? 'On' : 'Off'}
            </span>
          </button>
          {children}
        </div>
      )}
    </div>
  )
}

export default AudioSettingsButton
