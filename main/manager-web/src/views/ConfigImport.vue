<template>
  <div class="welcome">
    <HeaderBar />
    <div class="operation-bar">
      <h2 class="page-title">Config Import</h2>
    </div>
    <div class="main-wrapper">
      <div class="content-panel">
        <div class="content-area">
          <el-card shadow="never" class="config-card">
            <p class="intro">
              Upload a YAML file produced by the xiaozhi-esp32 setup wizard
              (<code>scripts/setup_and_flash.py</code>). The server maps
              <code>selected_module</code> entries to the default agent template
              and merges per-provider sections into
              <code>ai_model_config.config_json</code>. Only allow-listed
              <code>server.*</code> keys are applied to <code>sys_params</code>.
            </p>

            <el-upload action="" :auto-upload="false" :on-change="onFileChange"
              :on-remove="onFileRemove" :file-list="fileList" :limit="1"
              accept=".yaml,.yml" drag>
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">Drop YAML here or <em>click to browse</em></div>
              <div class="el-upload__tip" slot="tip">.yaml / .yml only, max 256 KB</div>
            </el-upload>

            <div class="options">
              <span class="opt-label">Mode:</span>
              <el-radio-group v-model="mode" :disabled="validating || applying">
                <el-radio label="merge">Merge (recommended)</el-radio>
                <el-radio label="replace">Replace</el-radio>
              </el-radio-group>
            </div>

            <div class="actions">
              <el-button :disabled="!selectedFile || validating" type="primary"
                :loading="validating" @click="validate">Validate (dry run)</el-button>
              <el-button :disabled="!canApply || applying" type="success"
                :loading="applying" @click="apply">Apply</el-button>
              <el-button @click="reset" :disabled="validating || applying">Reset</el-button>
            </div>

            <div v-if="result" class="result">
              <div class="result-banner" :class="{ done: !result.dryRun }">
                {{ result.dryRun ? 'Dry run: no changes persisted.' : 'Applied to the running server.' }}
                <span class="mode-chip">mode: {{ result.mode }}</span>
              </div>

              <h3>Will apply</h3>
              <el-table v-if="appliedRows.length" :data="appliedRows" size="mini" border>
                <el-table-column prop="group" label="Group" width="150" />
                <el-table-column prop="type" label="Type" width="100" />
                <el-table-column prop="provideCode" label="Provider / Key" width="220" />
                <el-table-column prop="detail" label="Detail" />
              </el-table>
              <p v-else class="muted">Nothing to apply.</p>

              <h3 v-if="result.skippedModules && result.skippedModules.length">Will skip</h3>
              <el-table v-if="result.skippedModules && result.skippedModules.length"
                :data="result.skippedModules" size="mini" border>
                <el-table-column prop="type" label="Type" width="100" />
                <el-table-column prop="provideCode" label="Provider" width="220" />
                <el-table-column prop="reason" label="Reason" />
              </el-table>

              <h3 v-if="result.validationErrors && result.validationErrors.length">Warnings</h3>
              <ul v-if="result.validationErrors && result.validationErrors.length" class="warnings">
                <li v-for="(msg, idx) in result.validationErrors" :key="idx">{{ msg }}</li>
              </ul>
            </div>
          </el-card>
        </div>
      </div>
    </div>
    <el-footer><version-footer /></el-footer>
  </div>
</template>

<script>
import Api from '@/apis/api';
import HeaderBar from '@/components/HeaderBar.vue';
import VersionFooter from '@/components/VersionFooter.vue';

export default {
  name: 'ConfigImport',
  components: { HeaderBar, VersionFooter },
  data() {
    return {
      selectedFile: null,
      fileList: [],
      mode: 'merge',
      validating: false,
      applying: false,
      result: null,
      canApply: false
    };
  },
  computed: {
    appliedRows() {
      if (!this.result) return [];
      const rows = [];
      (this.result.appliedModules || []).forEach(m => rows.push({
        group: 'selected_module',
        type: m.type,
        provideCode: m.provideCode,
        detail: 'modelId=' + m.modelId
      }));
      (this.result.appliedFields || []).forEach(m => rows.push({
        group: 'config fields',
        type: m.type,
        provideCode: m.provideCode,
        detail: (m.fields || []).join(', ')
      }));
      (this.result.appliedServerParams || []).forEach(code => rows.push({
        group: 'sys_params',
        type: '',
        provideCode: code,
        detail: ''
      }));
      return rows;
    }
  },
  methods: {
    onFileChange(file) {
      if (file && file.size > 256 * 1024) {
        this.$message.error({ message: 'File exceeds 256 KB limit', showClose: true });
        this.reset();
        return;
      }
      this.selectedFile = file.raw;
      this.fileList = [file];
      this.canApply = false;
      this.result = null;
    },
    onFileRemove() {
      this.reset();
    },
    reset() {
      this.selectedFile = null;
      this.fileList = [];
      this.result = null;
      this.canApply = false;
    },
    validate() {
      this.validating = true;
      Api.config.uploadConfig(
        { file: this.selectedFile, dryRun: true, mode: this.mode },
        ({ data }) => {
          this.validating = false;
          if (data.code === 0) {
            this.result = data.data;
            this.canApply = true;
            this.$message.success({ message: 'Validation OK. Review the diff and click Apply.', showClose: true });
          } else {
            this.canApply = false;
            this.$message.error({ message: data.msg || 'Validation failed', showClose: true });
          }
        },
        (res) => {
          this.validating = false;
          this.canApply = false;
          this.$message.error({
            message: (res && res.data && res.data.msg) || 'Validation failed',
            showClose: true
          });
        }
      );
    },
    apply() {
      this.$confirm(
        'Apply this config to the running server? This updates ai_model_config, ai_agent_template and sys_params rows and invalidates the server-config Redis cache.',
        'Confirm apply',
        { confirmButtonText: 'Apply', cancelButtonText: 'Cancel', type: 'warning' }
      ).then(() => {
        this.applying = true;
        Api.config.uploadConfig(
          { file: this.selectedFile, dryRun: false, mode: this.mode },
          ({ data }) => {
            this.applying = false;
            if (data.code === 0) {
              this.result = data.data;
              this.$message.success({ message: 'Config applied', showClose: true });
            } else {
              this.$message.error({ message: data.msg || 'Apply failed', showClose: true });
            }
          },
          (res) => {
            this.applying = false;
            this.$message.error({
              message: (res && res.data && res.data.msg) || 'Apply failed',
              showClose: true
            });
          }
        );
      }).catch(() => { });
    }
  }
};
</script>

<style lang="scss" scoped>
.welcome {
  min-width: 900px;
  min-height: 506px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(to bottom right, #dce8ff, #e4eeff, #e6cbfd);
}
.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
}
.page-title { font-size: 24px; margin: 0; color: #3d4566; }
.main-wrapper {
  margin: 0 22px 16px;
  border-radius: 15px;
  background: rgba(237, 242, 255, 0.5);
  flex: 1;
  display: flex;
  overflow: hidden;
}
.content-panel {
  flex: 1;
  border-radius: 15px;
  border: 1px solid #fff;
  background: white;
  overflow: auto;
}
.content-area { padding: 20px; }
.config-card { border: none; box-shadow: none; }
.intro { color: #606266; margin: 0 0 16px; line-height: 1.6; }
.intro code { background: #f0f2f5; padding: 1px 4px; border-radius: 3px; font-size: 12px; }
.options { margin: 16px 0; display: flex; align-items: center; gap: 12px; }
.opt-label { color: #606266; font-size: 13px; }
.actions { display: flex; gap: 10px; margin: 16px 0; }
.result { margin-top: 20px; }
.result-banner {
  padding: 10px 14px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.result-banner.done { background: #f0f9eb; color: #67c23a; }
.mode-chip { font-size: 12px; opacity: 0.7; }
.result h3 { margin: 18px 0 8px; color: #3d4566; font-size: 15px; }
.result .muted { color: #909399; font-size: 13px; }
.warnings { padding-left: 20px; color: #8a6d3b; margin: 8px 0; }
.warnings li { margin: 4px 0; }
</style>
