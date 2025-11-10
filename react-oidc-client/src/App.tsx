import React, { useEffect, useState } from 'react';
import { User } from 'oidc-client-ts';
import { handleCallback, login, logout } from './services/auth.client';

const App: React.FC = () => {
	const [user, setUser] = useState<User | null>(null);

	useEffect(() => {
		const handleAuth = async () => {
			// Eğer redirect ile geldiysen callback işlemini yap
			if (window.location.pathname === '/login/oauth2/code/react-oidc-client') {
				const loggedUser = await handleCallback();
				setUser(loggedUser);
				window.history.replaceState({}, document.title, '/'); // URL temizle
			}
		};
		handleAuth();
	}, []);

	return (
		<div style={{ padding: '2rem' }}>
			{!user && <button onClick={login}>Login with OAuth 2.0</button>}

			{user && (
				<div>
					<h2>Hoşgeldiniz, {user.profile?.sub}</h2>
					<pre>{JSON.stringify(user.profile, null, 2)}</pre>
					<button onClick={logout}>Logout</button>
				</div>
			)}
		</div>
	);
};

export default App;
