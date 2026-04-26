import { useState } from 'react';
import {
  clearStoredUserProfileImage,
  saveStoredUserProfileImage,
} from '../services/profile';
import './ProfileModal.css';

const AVATAR_MAX_SIZE = 240;

function resizeProfileImage(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = () => {
      const image = new Image();

      image.onload = () => {
        const scale = Math.min(1, AVATAR_MAX_SIZE / Math.max(image.width, image.height));
        const size = Math.max(1, Math.round(Math.max(image.width, image.height) * scale));
        const canvas = document.createElement('canvas');
        canvas.width = size;
        canvas.height = size;

        const context = canvas.getContext('2d');
        if (!context) {
          reject(new Error('Could not prepare image'));
          return;
        }

        const scaledWidth = Math.round(image.width * scale);
        const scaledHeight = Math.round(image.height * scale);
        const offsetX = Math.round((size - scaledWidth) / 2);
        const offsetY = Math.round((size - scaledHeight) / 2);

        context.fillStyle = '#f2e8cf';
        context.fillRect(0, 0, size, size);
        context.drawImage(image, offsetX, offsetY, scaledWidth, scaledHeight);
        resolve(canvas.toDataURL('image/jpeg', 0.86));
      };

      image.onerror = () => reject(new Error('Could not load image'));
      image.src = reader.result;
    };

    reader.onerror = () => reject(new Error('Could not read image'));
    reader.readAsDataURL(file);
  });
}

export default function ProfileModal({
  user,
  userProfile,
  fallbackImage,
  onClose,
  onProfileChange,
}) {
  const [profileUploadError, setProfileUploadError] = useState('');
  const profileImage = userProfile?.profileImage || fallbackImage;
  const profileStats = userProfile?.stats || { gamesPlayed: 0, wins: 0 };

  const handleProfileImageChange = async (event) => {
    const file = event.target.files?.[0];
    event.target.value = '';

    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      setProfileUploadError('Choose an image file.');
      return;
    }

    try {
      setProfileUploadError('');
      const imageDataUrl = await resizeProfileImage(file);
      onProfileChange(saveStoredUserProfileImage(user, imageDataUrl));
    } catch {
      setProfileUploadError('Could not save that image. Try a smaller file.');
    }
  };

  const handleRemoveProfileImage = () => {
    setProfileUploadError('');
    onProfileChange(clearStoredUserProfileImage(user));
  };

  return (
    <div className="profile-modal-overlay" onClick={onClose}>
      <div className="profile-modal-card" onClick={(event) => event.stopPropagation()}>
        <button
          className="profile-close-btn"
          type="button"
          onClick={onClose}
          aria-label="Close profile"
        >
          <span aria-hidden="true">×</span>
        </button>
        <h2 className="profile-modal-title">Profile</h2>
        <img className="profile-avatar-large profile-image" src={profileImage} alt="Profile avatar" />
        <div className="profile-upload-actions">
          <label className="profile-upload-btn">
            Upload image
            <input
              type="file"
              accept="image/*"
              onChange={handleProfileImageChange}
            />
          </label>
          {userProfile?.profileImage && (
            <button
              className="profile-remove-btn"
              type="button"
              onClick={handleRemoveProfileImage}
            >
              Remove
            </button>
          )}
        </div>
        {profileUploadError && <p className="profile-upload-error">{profileUploadError}</p>}
        <p className="profile-name">{user?.username || 'Guest'}</p>

        <div className="stats-section">
          <h3>Stats</h3>
          <div className="stat-row">
            <span>Games played</span>
            <strong>{Number(profileStats.gamesPlayed || 0)}</strong>
          </div>
          <div className="stat-row">
            <span>Wins</span>
            <strong>{Number(profileStats.wins || 0)}</strong>
          </div>
        </div>

        <button className="green-btn profile-done-btn" type="button" onClick={onClose}>
          Done
        </button>
      </div>
    </div>
  );
}
