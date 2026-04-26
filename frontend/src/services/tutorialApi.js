import axios from 'axios';
import { API_BASE_URL } from './authApi';

const headers = (userId) => ({ 'X-User-Id': String(userId) });

/** POST /api/tutorial/start — create tutorial game + bot, start round */
export async function startTutorial({ userId }) {
  const res = await axios.post(`${API_BASE_URL}/api/tutorial/start`, null, {
    headers: headers(userId),
  });
  return res.data;
}

/** GET /api/tutorial/{gameId}/state — re-derive current step from live state */
export async function getTutorialState({ gameId, userId }) {
  const res = await axios.get(`${API_BASE_URL}/api/tutorial/${gameId}/state`, {
    headers: headers(userId),
  });
  return res.data;
}

/** POST /api/tutorial/{gameId}/bot-flip — bot does its 2 initial flips */
export async function botFlip({ gameId, userId }) {
  const res = await axios.post(`${API_BASE_URL}/api/tutorial/${gameId}/bot-flip`, null, {
    headers: headers(userId),
  });
  return res.data;
}

/** POST /api/tutorial/{gameId}/bot-turn — bot executes a full random turn */
export async function botTurn({ gameId, userId }) {
  const res = await axios.post(`${API_BASE_URL}/api/tutorial/${gameId}/bot-turn`, null, {
    headers: headers(userId),
  });
  return res.data;
}
