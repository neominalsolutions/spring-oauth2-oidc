import { UserManager, WebStorageStateStore, User } from 'oidc-client-ts';
import { authConfig } from '../config/auth.config';

const settings = {
	authority: authConfig.authority,
	client_id: authConfig.clientId,
	redirect_uri: authConfig.redirectUri,
	post_logout_redirect_uri: authConfig.postLogoutRedirectUri,
	response_type: 'code',
	scope: authConfig.scopes.join(' '),
	userStore: new WebStorageStateStore({ store: window.localStorage }),
};

export const userManager = new UserManager(settings);

// Login başlat
export const login = async () => {
	await userManager.signinRedirect();
};

// Callback sonrası kullanıcıyı al
export const handleCallback = async (): Promise<User | null> => {
	const user = await userManager.signinRedirectCallback();
	return user;
};

// Logout başlat
export const logout = async () => {
	await userManager.signoutRedirect();
};

export async function getUser() {
	return await userManager.getUser();
}
