import { WorldState } from './modules/world';

export interface AuthState {
    token: string | null;
    isLoggedIn: boolean;
    username: string | null;
    email: string | null;
}

export interface RootState {
    auth: AuthState;
    world: WorldState;
}

export interface UserInfo {
    userName: string;
    email: string;
} 