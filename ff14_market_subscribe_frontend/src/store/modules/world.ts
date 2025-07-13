import { Module } from 'vuex';
import { RootState } from '../types';
import axios from '@/utils/axios';

export interface World {
    id: string;
    name: string;
    region: string;
    dataCenter: string;
}

export interface WorldState {
    worlds: World[];
}

const world: Module<WorldState, RootState> = {
    namespaced: true,
    state: {
        worlds: []
    },
    mutations: {
        SET_WORLDS(state, worlds: World[]) {
            state.worlds = worlds;
        }
    },
    actions: {
        async fetchWorlds({ commit, state }) {
            if (state.worlds.length > 0) {
                return state.worlds;
            }
            try {
                const response = await axios.get('/ff14/world');
                const worlds = response.data.data;
                commit('SET_WORLDS', worlds);
                return worlds;
            } catch (error) {
                console.error('Failed to fetch worlds:', error);
                // Return empty array on error
                return [];
            }
        }
    },
    getters: {
        getWorlds(state): World[] {
            return state.worlds;
        }
    }
};

export default world; 