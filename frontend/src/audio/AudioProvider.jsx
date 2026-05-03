import { useEffect, useMemo, useRef, useState } from 'react'
import { AudioContextValue } from './AudioContext'

const SOUND_STORAGE_KEY = 'sound_enabled'
const LEGACY_SFX_STORAGE_KEY = 'sfx_enabled'
const BACKGROUND_MUSIC_URL = 'https://assets.mixkit.co/music/1009/1009.mp3'
const BACKGROUND_MUSIC_PLAYBACK_RATE = 1
const MUSIC_ENABLED_PATHS = new Set(['/game-selection', '/join', '/play'])

/**
 * Reads the persisted sound setting, including the older storage key used by
 * previous builds.
 */
function readStoredSoundPreference() {
  if (typeof window === 'undefined') {
    return true
  }

  const stored = window.localStorage.getItem(SOUND_STORAGE_KEY)
  if (stored !== null) {
    return stored !== 'false'
  }

  const legacy = window.localStorage.getItem(LEGACY_SFX_STORAGE_KEY)
  if (legacy !== null) {
    return legacy !== 'false'
  }

  return true
}

function syncSoundPreference(enabled) {
  if (typeof window === 'undefined') {
    return
  }

  const value = enabled ? 'true' : 'false'
  window.localStorage.setItem(SOUND_STORAGE_KEY, value)
  window.localStorage.setItem(LEGACY_SFX_STORAGE_KEY, value)
}

function getOrCreatePlayer(playerRef) {
  if (playerRef.current || typeof window === 'undefined') {
    return playerRef.current
  }

  const music = new Audio(BACKGROUND_MUSIC_URL)
  music.preload = 'auto'
  music.loop = true
  music.volume = 0.1
  music.playbackRate = BACKGROUND_MUSIC_PLAYBACK_RATE

  playerRef.current = {
    music,
    sfxContext: null,
  }
  return playerRef.current
}

function getOrCreateSfxContext(playerRef) {
  const player = getOrCreatePlayer(playerRef)
  if (!player || typeof window === 'undefined') {
    return null
  }

  if (!player.sfxContext) {
    const AudioCtor = window.AudioContext || window.webkitAudioContext
    if (!AudioCtor) {
      return null
    }
    player.sfxContext = new AudioCtor()
  }

  return player.sfxContext
}

async function unlockSfxContext(playerRef) {
  const context = getOrCreateSfxContext(playerRef)
  if (!context) {
    return null
  }

  if (context.state !== 'running') {
    try {
      await context.resume()
    } catch {
      return null
    }
  }

  return context
}

function playTone(context, options) {
  if (!context || context.state !== 'running') {
    return
  }

  const {
    type = 'sine',
    frequency,
    endFrequency,
    duration = 0.2,
    volume = 0.05,
    startOffset = 0,
  } = options

  const startTime = context.currentTime + startOffset
  const oscillator = context.createOscillator()
  const gain = context.createGain()

  oscillator.type = type
  oscillator.frequency.setValueAtTime(frequency, startTime)
  if (endFrequency) {
    oscillator.frequency.exponentialRampToValueAtTime(endFrequency, startTime + duration)
  }

  gain.gain.setValueAtTime(0.0001, startTime)
  gain.gain.exponentialRampToValueAtTime(volume, startTime + 0.02)
  gain.gain.exponentialRampToValueAtTime(0.0001, startTime + duration)

  oscillator.connect(gain)
  gain.connect(context.destination)
  oscillator.start(startTime)
  oscillator.stop(startTime + duration + 0.04)
}

async function playSoundEffect(playerRef, soundName) {
  const context = await unlockSfxContext(playerRef)
  if (!context) {
    return
  }

  if (soundName === 'button-whoosh') {
    playTone(context, {
      type: 'triangle',
      frequency: 700,
      endFrequency: 260,
      duration: 0.12,
      volume: 0.055,
    })
    playTone(context, {
      type: 'sine',
      frequency: 450,
      endFrequency: 180,
      duration: 0.18,
      volume: 0.09,
      startOffset: 0.01,
    })
    return
  }

  if (soundName === 'turn-chime') {
    playTone(context, {
      type: 'sine',
      frequency: 784,
      endFrequency: 784,
      duration: 0.18,
      volume: 0.045,
    })
    playTone(context, {
      type: 'sine',
      frequency: 1174,
      endFrequency: 1174,
      duration: 0.24,
      volume: 0.03,
      startOffset: 0.08,
    })
  }
}

async function startBackgroundMusic(playerRef) {
  const player = getOrCreatePlayer(playerRef)
  if (!player) {
    return
  }

  try {
    await player.music.play()
  } catch {
    return
  }
}

function stopBackgroundMusic(playerRef) {
  const player = playerRef.current
  if (!player) {
    return
  }

  player.music.pause()
}

/**
 * Provides shared music and sound-effect controls for the React app.
 */
export function AudioProvider({ children }) {
  const [isSoundEnabled, setIsSoundEnabled] = useState(readStoredSoundPreference)
  const [currentPath, setCurrentPath] = useState(() =>
    typeof window === 'undefined' ? '/' : window.location.pathname
  )
  const playerRef = useRef(null)

  useEffect(() => {
    syncSoundPreference(isSoundEnabled)
  }, [isSoundEnabled])

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined
    }

    const updatePath = () => {
      setCurrentPath(window.location.pathname)
    }

    const originalPushState = window.history.pushState
    const originalReplaceState = window.history.replaceState

    window.history.pushState = function pushState(...args) {
      const result = originalPushState.apply(this, args)
      updatePath()
      return result
    }

    window.history.replaceState = function replaceState(...args) {
      const result = originalReplaceState.apply(this, args)
      updatePath()
      return result
    }

    window.addEventListener('popstate', updatePath)

    return () => {
      window.history.pushState = originalPushState
      window.history.replaceState = originalReplaceState
      window.removeEventListener('popstate', updatePath)
    }
  }, [])

  useEffect(() => {
    if (typeof window === 'undefined') {
      return undefined
    }

    const handleStartMusic = async () => {
      if (!isSoundEnabled || !MUSIC_ENABLED_PATHS.has(currentPath)) {
        return
      }

      await startBackgroundMusic(playerRef)
    }

    const interactionEvents = ['pointerdown', 'keydown', 'touchstart', 'click']
    interactionEvents.forEach((eventName) => {
      window.addEventListener(eventName, handleStartMusic, { passive: true })
    })

    return () => {
      interactionEvents.forEach((eventName) => {
        window.removeEventListener(eventName, handleStartMusic)
      })
    }
  }, [currentPath, isSoundEnabled])

  useEffect(() => {
    if (typeof document === 'undefined') {
      return undefined
    }

    const handleButtonClick = (event) => {
      if (!isSoundEnabled) {
        return
      }

      const target = event.target instanceof Element ? event.target : null
      if (!target?.closest('button')) {
        return
      }

      playSoundEffect(playerRef, 'button-whoosh')
    }

    document.addEventListener('click', handleButtonClick, true)
    return () => {
      document.removeEventListener('click', handleButtonClick, true)
    }
  }, [isSoundEnabled])

  useEffect(() => {
    if (!isSoundEnabled || !MUSIC_ENABLED_PATHS.has(currentPath)) {
      stopBackgroundMusic(playerRef)
      return
    }

    startBackgroundMusic(playerRef)
  }, [currentPath, isSoundEnabled])

  useEffect(() => () => {
    stopBackgroundMusic(playerRef)
    const sfxContext = playerRef.current?.sfxContext
    sfxContext?.close?.().catch(() => {})
  }, [])

  const value = useMemo(() => ({
    isSoundEnabled,
    setIsSoundEnabled,
    playSound: (soundName) => {
      if (!isSoundEnabled) {
        return
      }

      playSoundEffect(playerRef, soundName)
    },
  }), [isSoundEnabled])

  return (
    <AudioContextValue.Provider value={value}>
      {children}
    </AudioContextValue.Provider>
  )
}
