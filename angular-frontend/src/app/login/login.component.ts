import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  loading: boolean = false;
  error: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    if (!this.username || !this.password) {
      this.error = 'Por favor ingresa usuario y contraseña';
      return;
    }
    this.loading = true;
    this.error = '';

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('userId', res.id);
        localStorage.setItem('username', res.username);
        // Guardar cedula para usarla en compras y SaludPay
        localStorage.setItem('cedula', res.cedula || '');
        this.loading = false;
        this.router.navigate(['/catalog']);
      },
      error: (err: any) => {
        this.loading = false;
        this.error = 'Credenciales incorrectas. Intenta nuevamente.';
      }
    });
  }
}
