<template>
  <el-dialog v-dialogDrag class="setting" title="设置" :visible.sync="isShow" width="600px">
    <el-tabs v-model="tabValue" tab-position="left" style="min-height: 400px;" @tab-click="onClickTab">
      <el-tab-pane label="个人资料">
        <personal-info ref="personalInfo"></personal-info>
      </el-tab-pane>
      <el-tab-pane label="用户设置">
        <user-config ref="userConfig"></user-config>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script>
import PersonalInfo from './PersonalInfo.vue';
import UserConfig from './UserConfig.vue';

export default {
  name: "setting",
  components: {
    PersonalInfo,
    UserConfig
  },
  data() {
    return {
      isShow: false,
      tabValue: "0"
    }
  },
  methods: {
    open() {
      this.tabValue = "0";
      this.isShow = true;
      this.$nextTick(() => this.onClickTab())
    },
    onClickTab() {
      if (this.tabValue == "0") {
        this.$refs.personalInfo.init();
      }else if(this.tabValue == "1"){
        this.$refs.userConfig.init(); 
      }
    }
  },
  props: {
    visible: {
      type: Boolean
    }
  }

}
</script>

<style lang="scss"></style>
