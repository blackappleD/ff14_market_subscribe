import { createStore } from 'vuex';
import auth from './modules/auth';
import world from './modules/world';
import { RootState } from './types';

export default createStore<RootState>({
  modules: {
    auth,
    world
  }
}); 