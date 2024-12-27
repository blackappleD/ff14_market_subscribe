export interface AuthState {
    token: string | null;
    isLoggedIn: boolean;
    username: string | null;
    email: string | null;
}

export interface RootState {
    auth: AuthState;
}

export interface UserInfo {
    userName: string;
    email: string;
} 