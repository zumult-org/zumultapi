// the application is started here, with all the 
// necessary frameworks and libraries imported
import Vue from 'vue'
import App from './App.vue'
import store from './store/'
import {BootstrapVue, BootstrapVueIcons} from 'bootstrap-vue'
import 'bootstrap/dist/css/bootstrap.min.css'
import '@/assets/css/all.min.css'
import 'bootstrap/dist/js/bootstrap.min.js'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import 'jquery/dist/jquery.min.js'

Vue.use(BootstrapVue)
Vue.use(BootstrapVueIcons)

Vue.config.productionTip = false

export default new Vue({
    store,
    render: h => h(App),
}).$mount('#app')
