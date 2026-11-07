<template>
  <div class="register-page">
    <NavBar/>

    <main class="register-main">
      <div class="register-card">
        <h2 class="title">开户申请</h2>

        <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            size="large"
            @submit.prevent="submitRegister"
        >
          <el-form-item label="姓名" prop="name">
            <el-input v-model.trim="form.name" placeholder="请输入姓名" maxlength="20"/>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入6位数字密码"
                maxlength="6"
                inputmode="numeric"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirm">
            <el-input
                v-model="form.confirm"
                type="password"
                show-password
                placeholder="再次输入密码"
                maxlength="6"
                inputmode="numeric"
            />
          </el-form-item>

          <el-form-item label="性别" prop="sex">
            <el-select v-model="form.sex" placeholder="请选择" class="full">
              <el-option label="男" value="男"/>
              <el-option label="女" value="女"/>
            </el-select>
          </el-form-item>

          <el-form-item label="每日限额（元）" prop="dailyLimit">
            <el-input-number
                v-model="form.dailyLimit"
                :min="LIMIT_MIN"
                :max="LIMIT_MAX"
                :step="100"
                :precision="0"
                controls-position="right"
                class="full"
            />
            <div class="hint">每日取款和转账的支出上限，可设置 {{ LIMIT_MIN }} 到 {{ LIMIT_MAX }} 元</div>
          </el-form-item>

          <el-alert
              v-if="msg"
              :title="msg"
              type="error"
              show-icon
              :closable="false"
              class="msg"
          />

          <el-button
              type="primary"
              native-type="submit"
              :loading="submitting"
              class="full"
          >
            提交开户申请
          </el-button>

          <el-button class="full secondary" @click="$router.push('/login')">返回登录</el-button>
        </el-form>
      </div>
    </main>
  </div>
</template>

<script>
import request from "@/utils/request";
import NavBar from "@/components/NavBar.vue";
import {encryptPassword} from "@/assets/scripts/encryption";
import {
  ElAlert,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessageBox,
  ElOption,
  ElSelect
} from "element-plus";

// 限额范围只是前端的输入限制，后端的 register 也要用同样的范围校验
const LIMIT_DEFAULT = 20000;
const LIMIT_MIN = 100;
const LIMIT_MAX = 50000;

export default {
  components: {
    NavBar,
    ElAlert,
    ElButton,
    ElForm,
    ElFormItem,
    ElInput,
    ElInputNumber,
    ElOption,
    ElSelect
  },

  data() {
    const validateConfirm = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请再次输入密码"));
      } else if (value !== this.form.password) {
        callback(new Error("两次密码不一致"));
      } else {
        callback();
      }
    };

    const validateLimit = (rule, value, callback) => {
      if (value === null || value === undefined || value === "") {
        callback(new Error("请输入每日限额"));
      } else if (value < LIMIT_MIN || value > LIMIT_MAX) {
        callback(new Error(`限额范围是 ${LIMIT_MIN} 到 ${LIMIT_MAX} 元`));
      } else {
        callback();
      }
    };

    return {
      LIMIT_MIN,
      LIMIT_MAX,
      form: {
        name: "",
        password: "",
        confirm: "",
        sex: "",
        dailyLimit: LIMIT_DEFAULT
      },
      rules: {
        name: [
          {required: true, whitespace: true, message: "请输入姓名", trigger: "blur"},
          {max: 20, message: "姓名不超过 20 个字", trigger: "blur"}
        ],
        password: [
          {required: true, message: "请输入密码", trigger: "blur"},
          {pattern: /^\d{6}$/, message: "密码必须是 6 位数字", trigger: "blur"}
        ],
        confirm: [{required: true, message: "请再次输入密码", validator: validateConfirm, trigger: ["blur", "change"]}],
        sex: [{required: true, message: "请选择性别", trigger: "change"}],
        dailyLimit: [{validator: validateLimit, trigger: ["blur", "change"]}]
      },
      msg: "",
      submitting: false
    };
  },

  watch: {
    // 改了密码后，如果确认密码已经填过，重新校验一次
    "form.password"() {
      if (this.form.confirm) {
        this.$refs.formRef?.validateField("confirm").catch(() => {
        });
      }
    }
  },

  methods: {
    async submitRegister() {
      this.msg = "";

      try {
        await this.$refs.formRef.validate();
      } catch (e) {
        return; // 校验不通过，错误提示已显示在对应字段下
      }

      this.submitting = true;
      try {
        const res = await request.post("/register", {
          name: this.form.name,
          password: encryptPassword(this.form.password),
          dailyLimit: this.form.dailyLimit,
          sex: this.form.sex
        });

        const acc = res.data.data; // 后端返回完整 account 对象

        await ElMessageBox.alert(
            `您的卡号是：${acc.card}。请妥善保管，登录时需要使用此卡号。`,
            "开户成功",
            {confirmButtonText: "进入首页"}
        );

        this.$router.push("/home");
      } catch (e) {
        // 用户在弹窗里关闭时 e 是字符串 "close" 或 "cancel"，仍然进入首页
        if (e === "close" || e === "cancel") {
          this.$router.push("/home");
          return;
        }
        this.msg = e.message || "服务器错误";
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped>
/* 整页纵向排列：导航栏在上，剩余空间里把卡片水平、垂直居中 */
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--el-fill-color-light);
}

.register-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  padding: 28px 28px 24px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);
}

.title {
  margin: 0 0 20px;
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.full {
  width: 100%;
}

.secondary {
  margin: 12px 0 0;
}

.hint {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--el-text-color-secondary);
}

.msg {
  margin-bottom: 16px;
}
</style>