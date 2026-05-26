import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  username: string = '';
  email: string = '';
  password: string = '';
  cedula: string = '';
  loading: boolean = false;
  error: string = '';
  successMessage: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    if (!this.username || !this.email || !this.password || !this.cedula) {
      this.error = 'Todos los campos son obligatorios';
      return;
    }
    this.loading = true;
    this.error = '';
    this.successMessage = '';

    const registerData = {
      username: this.username,
      email: this.email,
      password: this.password,
      cedula: this.cedula
    };

    this.authService.register(registerData).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.successMessage = 'Usuario registrado con exito. Redirigiendo al login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err: any) => {
        this.loading = false;
        if (err.error && typeof err.error === 'string') {
          this.error = err.error;
        } else if (err.error && err.error.message) {
          this.error = err.error.message;
        } else {
          this.error = 'Error al registrar el usuario. Por favor intenta nuevamente.';
        }
      }
    });
  }
}
