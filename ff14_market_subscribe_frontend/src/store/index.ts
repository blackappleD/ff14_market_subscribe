import { createStore } from 'vuex'
import auth from './modules/auth'
import { RootState } from './types'

export default createStore<RootState>({
  modules: {
    auth
  }
}) 