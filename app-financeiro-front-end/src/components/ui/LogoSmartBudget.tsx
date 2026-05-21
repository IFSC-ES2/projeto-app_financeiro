import React from 'react';

interface Props {
  size?: number;
}

const LogoSmartBudget: React.FC<Props> = ({ size = 32 }) => (
  <svg width={size} height={size} viewBox="0 0 36 36" fill="none">
    <rect width="36" height="36" rx="10" fill="#2FA98F" />
    <path
      d="M10 24 L14 16 L18 20 L22 12 L26 18"
      stroke="white"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="none"
    />
    <circle cx="26" cy="18" r="2" fill="white" />
  </svg>
);

export default LogoSmartBudget;
